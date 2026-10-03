package com.telegramdrive.uploader.domain.model

enum class TelegramDestinationType {
    USER,
    GROUP,
    SUPERGROUP,
    CHANNEL,
    OTHER
}

data class TelegramDestination(
    val id: Long,
    val title: String,
    val username: String?,
    val type: TelegramDestinationType,
    val photo: String?,
    val canSendMessages: Boolean
)
