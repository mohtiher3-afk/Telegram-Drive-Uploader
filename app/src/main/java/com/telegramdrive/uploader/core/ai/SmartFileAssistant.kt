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
    private val arabicRules = mapOf(
        "شاشة" to "تسجيل شاشة",
        "تسجيل" to "تسجيل",
        "اجتماع" to "اجتماع",
        "محاضرة" to "تعليم",
        "درس" to "تعليم",
        "سفر" to "سفر",
        "كاميرا" to "كاميرا",
        "فيديو" to "فيديو"
    )
    private val latinRules = mapOf(
        "screen" to "screen-recording",
        "record" to "screen-recording",
        "meeting" to "meeting",
        "lecture" to "education",
        "camera" to "camera",
        "travel" to "travel"
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
            .take(4)
            .joinToString("_")
            .ifBlank { "video" }
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(task.createdAt))
        val dimensions = if (task.width > 0 && task.height > 0) {
            "${task.width}x${task.height}"
        } else null
        val suggestedName = listOfNotNull(date, base, dimensions)
            .joinToString("_") + "." + extension

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
}
