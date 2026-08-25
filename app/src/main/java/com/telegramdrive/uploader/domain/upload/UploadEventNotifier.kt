package com.telegramdrive.uploader.domain.upload

/** Posts a user-visible observation of a persisted terminal upload event. */
interface UploadEventNotifier {
    fun notify(event: UploadEventNotificationEvent, uploadId: String)
}
