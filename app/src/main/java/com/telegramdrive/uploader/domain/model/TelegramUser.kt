package com.telegramdrive.uploader.domain.model

data class TelegramUser(
    val id: Long,
    val firstName: String,
    val lastName: String?,
    val username: String?,
    val phoneNumber: String,
    val profilePhoto: String?
)
