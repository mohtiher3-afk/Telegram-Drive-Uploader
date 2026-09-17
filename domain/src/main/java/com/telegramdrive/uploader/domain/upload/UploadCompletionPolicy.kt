package com.telegramdrive.uploader.domain.upload

/**
 * Completion is deliberately fail-closed: a local stream ending is not delivery.
 */
object UploadCompletionPolicy {
    enum class Decision { CONFIRMED, UNCONFIRMED }

    fun decide(hasConfirmedTelegramDelivery: Boolean): Decision =
        if (hasConfirmedTelegramDelivery) Decision.CONFIRMED else Decision.UNCONFIRMED
}

