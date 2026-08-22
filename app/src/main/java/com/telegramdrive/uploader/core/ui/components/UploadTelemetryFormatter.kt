package com.telegramdrive.uploader.core.ui.components

import java.util.Locale

fun formatTransferSpeed(bytesPerSecond: Long): String {
    if (bytesPerSecond <= 0L) return "—"
    return "${formatFileSize(bytesPerSecond)}/s"
}

fun formatRemainingTime(seconds: Long): String {
    if (seconds <= 0L) return ""
    val minutes = seconds / 60L
    val remainingSeconds = seconds % 60L
    return if (minutes > 0L) {
        "%dm %02ds".format(Locale.US, minutes, remainingSeconds)
    } else {
        "%ds".format(Locale.US, remainingSeconds)
    }
}
