package com.telegramdrive.uploader.domain.model

/**
 * Guards the boundary between a displayed Telegram destination and an upload task.
 * A destination must be real, supported by the upload path, and currently sendable.
 */
object TelegramDestinationPolicy {
    fun isSelectable(destination: TelegramDestination): Boolean =
        destination.id != 0L &&
            destination.canSendMessages &&
            destination.type != TelegramDestinationType.OTHER
}