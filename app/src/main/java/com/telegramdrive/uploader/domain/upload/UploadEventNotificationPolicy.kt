package com.telegramdrive.uploader.domain.upload

import com.telegramdrive.uploader.domain.model.UploadStatus

enum class UploadEventNotificationEvent {
    COMPLETED,
    FAILED
}

/** Keeps notifications subordinate to persisted, terminal upload state. */
object UploadEventNotificationPolicy {
    fun eventFor(status: UploadStatus): UploadEventNotificationEvent? = when (status) {
        UploadStatus.COMPLETED -> UploadEventNotificationEvent.COMPLETED
        UploadStatus.FAILED -> UploadEventNotificationEvent.FAILED
        else -> null
    }
}
