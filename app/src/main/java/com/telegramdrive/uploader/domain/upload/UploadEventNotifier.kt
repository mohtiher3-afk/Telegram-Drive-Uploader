package com.telegramdrive.uploader.domain.upload

import android.app.Notification

/** Posts a user-visible observation of a persisted terminal upload event. */
interface UploadEventNotifier {
    fun notify(event: UploadEventNotificationEvent, uploadId: String)
    fun showProgressNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long)
    fun dismissProgressNotification(uploadId: String)

    /** Builds a Notification to power a foreground service during an active upload. */
    fun buildForegroundNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long): Notification
}
