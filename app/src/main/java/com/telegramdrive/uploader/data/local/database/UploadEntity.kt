package com.telegramdrive.uploader.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "uploads")
data class UploadEntity(
    @PrimaryKey val id: String,
    val sourceUri: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val destinationId: Long,
    val status: String,
    val progress: Float,
    val uploadedBytes: Long,
    val totalBytes: Long,
    val speed: Long,
    val averageSpeed: Long,
    val eta: Long,
    val createdAt: Long,
    val startedAt: Long?,
    val completedAt: Long?,
    val lastError: String?,
    val retryCount: Int,
    val thumbnailPath: String?,
    val duration: Long,
    val width: Int,
    val height: Int,
    val scheduledAt: Long? = null,
    val uploadDurationMs: Long = 0L,
    val messageLink: String? = null,
    /**
     * Provisional TDLib message id from SendMessage, persisted the moment the send
     * call returns — before Telegram confirms. On worker retry, a non-null value
     * means "already sent, await confirmation" and the engine must NEVER blind-resend
     * (that would duplicate the Telegram message).
     */
    val provisionalMessageId: Long? = null,
    /**
     * Durable "send dispatched" marker, persisted BEFORE the SendMessage call reaches
     * TDLib. If the process dies between dispatch and the provisional-id persist, a
     * retry still sees this flag and must resolve via the confirmation/history path
     * instead of re-sending (which would duplicate the Telegram message).
     */
    val sendDispatched: Boolean = false,
    /**
     * Final (post-confirmation) TDLib message id, persisted when delivery is
     * confirmed. A non-null value means the upload already completed delivery and
     * retries must never touch TDLib again.
     */
    val finalMessageId: Long? = null
)
