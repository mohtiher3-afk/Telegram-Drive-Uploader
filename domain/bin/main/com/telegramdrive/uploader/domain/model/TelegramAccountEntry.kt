package com.telegramdrive.uploader.domain.model

data class TelegramAccountEntry(
    val key: String,
    val phone: String,
    val displayName: String,
    val isActive: Boolean = false,
    /**
     * TDLib database/files directory suffix (`filesDir/tdlib-database-<dirKey>`)
     * this account's session was created under. Recorded at first login so the
     * directory stays stable across process restarts; defaults to [key] for rows
     * stored before this field existed.
     */
    val dirKey: String = key
)