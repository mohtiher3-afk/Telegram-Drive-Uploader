package com.telegramdrive.uploader.core.ai

import com.telegramdrive.uploader.domain.model.UploadTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local fallback assistant. It never blocks upload and does not inspect file contents.
 * Arabic letters and numbers are preserved in suggested filenames.
 */
data class SmartFileSuggestion(
    val taskId: String,
    val suggestedName: String,
    val keywords: List<String>
)

object SmartFileAssistant {
    private val separatorPattern = Regex("[^\\p{L}\\p{N}]+")
    private val forbiddenFilenameChars = Regex("[\\\\/:*?\"<>|]")
    private const val MAX_BASE_WORDS = 4
    private const val MAX_BASE_LENGTH = 60
    private const val MAX_FILENAME_LENGTH = 120
    // Non-ASCII filenames should be truncated on whole characters, not by byte count.
    private val arabicRules = mapOf(
        "شاشة" to "تسجيل شاشة",
        "تسجيل" to "تسجيل",
        "اجتماع" to "اجتماع",
        "محاضرة" to "تعليم",
        "درس" to "تعليم",
        "شرح" to "تعليم",
        "ملخص" to "ملخص",
        "تطبيق" to "تطبيق",
        "تصميم" to "تصميم",
        "عرض تقديمي" to "عرض تقديمي",
        "سفر" to "سفر",
        "كاميرا" to "كاميرا",
        "فيديو" to "فيديو"
    )
    private val latinRules = mapOf(
        "screen" to "screen-recording",
        "record" to "screen-recording",
        "capture" to "screen-recording",
        "meeting" to "meeting",
        "zoom" to "meeting",
        "lecture" to "education",
        "lesson" to "education",
        "tutorial" to "education",
        "course" to "education",
        "camera" to "camera",
        "travel" to "travel",
        "demo" to "demo",
        "presentation" to "presentation",
        "webinar" to "meeting",
        "clip" to "clip"
    )

    fun suggest(task: UploadTask): SmartFileSuggestion {
        val extension = task.fileName.substringAfterLast('.', "mp4")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]"), "")
            .ifBlank { "mp4" }
        val base = task.fileName.substringBeforeLast('.', task.fileName)
            .replace(separatorPattern, " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.length > 1 }
            .take(MAX_BASE_WORDS)
            .joinToString("_")
            .ifBlank { "video" }
            .let { base ->
                if (base.length > MAX_BASE_LENGTH) base.take(MAX_BASE_LENGTH) else base
            }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(task.createdAt))
        val dimensions = if (task.width > 0 && task.height > 0) {
            "${task.width}x${task.height}"
        } else null
        val core = listOfNotNull(date, base, dimensions).joinToString("_")
        val suggestedName = sanitizeFilename(core, extension)

        val source = task.fileName.substringBeforeLast('.', task.fileName)
        val lowerSource = source.lowercase(Locale.ROOT)
        val inferred = buildList {
            arabicRules.forEach { (term, keyword) ->
                if (source.contains(term, ignoreCase = true)) add(keyword)
            }
            latinRules.forEach { (term, keyword) ->
                if (lowerSource.contains(term)) add(keyword)
            }
        }
        val keywords = buildList {
            add(if (source.any { it in '\u0600'..'\u06ff' }) "فيديو" else "video")
            addAll(inferred)
            if (task.width > task.height && task.width > 0) add("أفقي")
            if (task.height > task.width && task.height > 0) add("عمودي")
            if (task.duration > 0) add("وسائط")
        }.distinct().take(4)

        return SmartFileSuggestion(task.id, suggestedName, keywords)
    }

    /**
     * Builds a safe, length-capped filename of the form "<core>.<extension>". Strips any
     * leftover filesystem-invalid characters and truncates on whole characters so that
     * Arabic/multibyte names are never cut in the middle of a code point.
     */
    private fun sanitizeFilename(core: String, extension: String): String {
        // Preserve hyphens (used in the date prefix) while collapsing other separators.
        val hyphenSafe = Regex("[^\\p{L}\\p{N}-]+")
        val cleaned = core
            .replace(forbiddenFilenameChars, "_")
            .replace(hyphenSafe, "_")
            .trim('_', ' ')
            .ifBlank { "video" }
        val maxBase = MAX_FILENAME_LENGTH - extension.length - 1
        val truncated = if (cleaned.length > maxBase) cleaned.take(maxBase) else cleaned
        return "$truncated.$extension"
    }
}
