package com.telegramdrive.uploader.core.navigation

/**
 * Canonical route strings for the existing navigation graph.
 *
 * Values are intentionally unchanged because routes may be persisted or referenced
 * by external navigation state. This object centralizes definitions only.
 */
object AppRoutes {
    const val HOME = "home"
    const val QUEUE = "queue"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val UPLOAD_PREPARATION = "upload_preparation"
    const val TELEGRAM_AUTH = "telegram_auth"
    const val TELEGRAM_DESTINATION = "telegram_destination"
}
