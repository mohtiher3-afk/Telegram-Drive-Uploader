package com.telegramdrive.uploader.core.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig
import com.telegramdrive.uploader.domain.model.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * AI-powered Smart File Assistant using ML Kit GenAI Prompt API (Gemini Nano).
 *
 * This assistant analyzes image content using on-device Gemini Nano to suggest
 * meaningful filenames and keywords. It falls back to the deterministic
 * [SmartFileAssistant] when Gemini Nano is unavailable or fails.
 *
 * Key properties:
 * - On-device inference: no network required, no API keys, no cost
 * - Privacy-first: image content never leaves the device
 * - Graceful degradation: falls back to deterministic assistant on any failure
 *
 * Note: Requires Android API 26+ and AICore with Gemini Nano support.
 */
class GeminiSmartFileAssistant(private val context: Context) {

    companion object {
        private const val MAX_IMAGE_DIMENSION = 1024
        private val PROMPT_TEMPLATE: String = """
            Analyze this image and suggest a concise filename (max 50 chars, use underscores instead of spaces) 
            and up to 4 relevant keywords that describe the content.
            
            Respond in this exact format:
            FILENAME: <suggested_filename>
            KEYWORDS: <keyword1>, <keyword2>, <keyword3>, <keyword4>
            
            Keep the filename descriptive but short. Keywords should be in the language of the content.
        """.trimIndent()
    }

    private var generativeModel: com.google.mlkit.genai.prompt.GenerativeModel? = null
    private var isModelReady = false

    /**
     * Initialize the Gemini Nano model.
     * Should be called before [suggest] to ensure the model is available.
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val config = generationConfig {
                modelConfig = modelConfig {
                    releaseStage = ModelReleaseStage.PREVIEW
                    preference = ModelPreference.FAST
                }
            }

            generativeModel = Generation.getClient(config)

            // Check if model is available (FeatureStatus.AVAILABLE = 1)
            val status = generativeModel?.checkStatus()
            isModelReady = status == 1
            isModelReady
        } catch (e: Exception) {
            isModelReady = false
            false
        }
    }

    /**
     * Check if the Gemini Nano model is available and ready.
     */
    fun isReady(): Boolean = isModelReady

    /**
     * Suggest a filename and keywords for the given upload task.
     *
     * If the task has an image thumbnail and Gemini Nano is available,
     * uses AI to analyze the image content. Otherwise falls back to
     * the deterministic [SmartFileAssistant].
     */
    suspend fun suggest(task: UploadTask): SmartFileSuggestion = withContext(Dispatchers.IO) {
        // Try AI-powered suggestion if we have a thumbnail and model is ready
        if (isModelReady && task.thumbnailPath != null) {
            try {
                val aiSuggestion = suggestWithAI(task)
                if (aiSuggestion != null) {
                    return@withContext aiSuggestion
                }
            } catch (e: Exception) {
                // Fall through to deterministic assistant
            }
        }

        // Fallback to deterministic assistant
        SmartFileAssistant.suggest(task)
    }

    /**
     * Use Gemini Nano to analyze image and suggest filename/keywords.
     */
    private suspend fun suggestWithAI(task: UploadTask): SmartFileSuggestion? {
        val thumbnailPath = task.thumbnailPath ?: return null
        val bitmap = loadBitmap(thumbnailPath) ?: return null

        val request = GenerateContentRequest.builder(
            ImagePart(bitmap),
            TextPart(PROMPT_TEMPLATE)
        ).build()

        val response = generativeModel?.generateContent(request) ?: return null
        val text = response.candidates.firstOrNull()?.text ?: return null

        return parseAIResponse(task.id, text, task.fileName)
    }

    /**
     * Load and resize bitmap from file path.
     */
    private fun loadBitmap(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) return null

            // Decode bounds first
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)

            // Calculate sample size
            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > MAX_IMAGE_DIMENSION) {
                sampleSize *= 2
            }

            // Decode with sample size
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeFile(path, decodeOptions)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse the AI response into a SmartFileSuggestion.
     */
    fun parseAIResponseForTest(taskId: String, response: String, originalFileName: String): SmartFileSuggestion {
        return parseAIResponse(taskId, response, originalFileName)
    }

    private fun parseAIResponse(taskId: String, response: String, originalFileName: String): SmartFileSuggestion {
        val lines = response.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var suggestedName = ""
        val keywords = mutableListOf<String>()

        for (line in lines) {
            when {
                line.startsWith("FILENAME:", ignoreCase = true) -> {
                    suggestedName = line.substringAfter("FILENAME:").trim()
                }
                line.startsWith("KEYWORDS:", ignoreCase = true) -> {
                    val kwText = line.substringAfter("KEYWORDS:").trim()
                    keywords.addAll(kwText.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }
            }
        }

        // If AI didn't provide a filename, fall back to original
        if (suggestedName.isBlank()) {
            val extension = originalFileName.substringAfterLast('.', "mp4")
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]"), "")
                .ifBlank { "mp4" }
            suggestedName = "image.$extension"
        }

        // Sanitize filename
        suggestedName = sanitizeFilename(suggestedName)

        // Limit keywords to 4
        val limitedKeywords = keywords.take(4)

        return SmartFileSuggestion(taskId, suggestedName, limitedKeywords)
    }

    /**
     * Sanitize filename to be safe for all filesystems.
     */
    private fun sanitizeFilename(name: String): String {
        val forbiddenChars = Regex("[\\\\/:*?\"<>|]")
        val cleaned = name
            .replace(forbiddenChars, "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06ff.]"), "")
            .take(120)
            .trim('_', '.')

        return if (cleaned.isBlank()) "image" else cleaned
    }

    /**
     * Release resources when done.
     */
    fun close() {
        generativeModel = null
        isModelReady = false
    }
}
