package com.telegramdrive.uploader.core.ai

import java.util.Locale

/**
 * Parses the raw text returned by the on-device Gemini Nano prompt into a
 * [SmartFileSuggestion].
 *
 * This lives outside [GeminiSmartFileAssistant] on purpose. The assistant needs an
 * Android [android.content.Context] to talk to ML Kit, so it cannot be instantiated in a
 * plain JVM unit test — which previously led the test to carry its own copy of the parsing
 * logic. That copy drifted from production and hid a real defect (the suggested extension
 * was never attached when the model supplied a filename). Keeping the parser a pure
 * function means the test and the assistant execute the exact same code.
 */
internal object SmartFileResponseParser {

    private const val MAX_FILENAME_LENGTH = 120
    private const val DEFAULT_EXTENSION = "mp4"
    private const val MAX_KEYWORDS = 4
    private val FORBIDDEN_CHARS = Regex("[\\\\/:*?\"<>|]")
    private val WHITESPACE = Regex("\\s+")
    private val DISALLOWED_CHARS = Regex("[^a-zA-Z0-9_\\-\\u0600-\\u06ff.]")

    /**
     * @param taskId identifier carried through to the suggestion.
     * @param response raw model output, expected to contain `FILENAME:` and `KEYWORDS:` lines.
     * @param originalFileName the file being uploaded. Its extension is authoritative: the
     *   model names the content, never the container, so any extension it guessed is dropped.
     */
    fun parse(taskId: String, response: String, originalFileName: String): SmartFileSuggestion {
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

        val extension = originalFileName.substringAfterLast('.', DEFAULT_EXTENSION)
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
            .ifBlank { DEFAULT_EXTENSION }

        val base = if (suggestedName.isBlank()) {
            "image"
        } else {
            suggestedName.substringBeforeLast('.', suggestedName)
        }

        val sanitizedBase = sanitize(base, MAX_FILENAME_LENGTH - extension.length - 1)
        val finalName = "$sanitizedBase.$extension"

        return SmartFileSuggestion(taskId, finalName, keywords.take(MAX_KEYWORDS))
    }

    /** Strips filesystem-hostile characters and caps the base name on whole characters. */
    private fun sanitize(name: String, maxLength: Int): String {
        val cleaned = name
            .replace(FORBIDDEN_CHARS, "_")
            .replace(WHITESPACE, "_")
            .replace(DISALLOWED_CHARS, "")
            .take(maxLength.coerceAtLeast(1))
            .trim('_', '.')

        return if (cleaned.isBlank()) "image" else cleaned
    }
}
