package com.telegramdrive.uploader.core.util

import android.webkit.MimeTypeMap
import java.util.Locale

object VideoFormatSupport {
    private val videoExtensions = setOf(
        "mp4", "m4v", "mkv", "webm", "mov", "qt", "avi", "3gp", "3gpp", "3g2",
        "ts", "m2ts", "mts", "mpg", "mpeg", "mpe", "flv", "wmv", "asf", "ogv"
    )

    fun normalizeMimeType(reportedMimeType: String?, fileName: String): String {
        val reported = reportedMimeType?.trim()?.lowercase(Locale.US).orEmpty()
        if (reported.startsWith("video/")) return reported
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        val guessed = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.lowercase(Locale.US)
        if (guessed?.startsWith("video/") == true) return guessed
        return when (extension) {
            "mkv" -> "video/x-matroska"
            "ts", "m2ts", "mts" -> "video/mp2t"
            "avi" -> "video/x-msvideo"
            "mov", "qt" -> "video/quicktime"
            "webm" -> "video/webm"
            "3gp", "3gpp" -> "video/3gpp"
            "3g2" -> "video/3gpp2"
            "flv" -> "video/x-flv"
            "wmv", "asf" -> "video/x-ms-wmv"
            "ogv" -> "video/ogg"
            else -> reported
        }
    }

    fun isSupportedVideo(mimeType: String, fileName: String): Boolean {
        val normalized = normalizeMimeType(mimeType, fileName)
        if (normalized.startsWith("video/")) return true
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        return extension in videoExtensions
    }
}
