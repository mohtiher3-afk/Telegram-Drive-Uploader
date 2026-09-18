package com.telegramdrive.uploader.domain.model

import java.util.Locale

/**
 * Guards the boundary between a displayed Telegram destination and an upload task.
 * A destination must be real, supported by the upload path, and currently sendable.
 */
object TelegramDestinationPolicy {
    fun isSelectable(destination: TelegramDestination): Boolean =
        destination.id != 0L &&
            destination.canSendMessages &&
            destination.type != TelegramDestinationType.OTHER

    /**
     * True when [destination] matches [query] after normalizing case, hyphens, spaces,
     * and a leading '@'. Channel titles like "Telegram-Drive-Uploader" are matched by
     * space-separated queries like "Telegram Drive Uploader".
     */
    fun matchesSearch(destination: TelegramDestination, query: String): Boolean {
        val normalized = query.trim().lowercase(Locale.US).removePrefix("@")
            .replace("-", "").replace(" ", "")
        if (normalized.isBlank()) return true
        val title = destination.title.lowercase(Locale.US).replace("-", "").replace(" ", "")
        val username = destination.username?.lowercase(Locale.US)?.replace("-", "")?.replace(" ", "")
        return title.contains(normalized) || username?.contains(normalized) == true
    }
}