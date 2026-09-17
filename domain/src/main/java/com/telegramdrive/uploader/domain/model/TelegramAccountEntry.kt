package com.telegramdrive.uploader.domain.model

data class TelegramAccountEntry(
    val key: String,
    val phone: String,
    val displayName: String,
    val isActive: Boolean = false
)