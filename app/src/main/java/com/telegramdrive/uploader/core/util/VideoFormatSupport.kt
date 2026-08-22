package com.telegramdrive.uploader.core.util

import java.util.Locale

object VideoFormatSupport {
    private val videoMimeByExtension = mapOf(
        "mp4" to "video/mp4", "m4v" to "video/x-m4v", "mkv" to "video/x-matroska",
        "webm" to "video/webm", "mov" to "video/quicktime", "qt" to "video/quicktime",
        "avi" to "video/x-msvideo", "3gp" to "video/3gpp", "3gpp" to "video/3gpp",
        "3g2" to "video/3gpp2", "ts" to "video/mp2t", "m2ts" to "video/mp2t",
        "mts" to "video/mp2t", "mpg" to "video/mpeg", "mpeg" to "video/mpeg",
        "mpe" to "video/mpeg", "flv" to "video/x-flv", "wmv" to "video/x-ms-wmv",
        "asf" to "video/x-ms-wmv", "ogv" to "video/ogg"
    )

    fun normalizeMimeType(reportedMimeType: String?, fileName: String): String {
        val reported = reportedMimeType?.trim()?.lowercase(Locale.US).orEmpty()
        if (reported.startsWith("video/")) return reported
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        val guessed = videoMimeByExtension[extension]
        if (guessed != null) return guessed
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
        return extension in videoMimeByExtension
    }
}
