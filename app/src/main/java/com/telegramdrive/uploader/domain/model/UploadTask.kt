package com.telegramdrive.uploader.domain.model

data class UploadTask(
    val id: String,
    val sourceUri: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String = "video/mp4",
    val destinationId: Long = 0L,
    val status: UploadStatus = UploadStatus.QUEUED,
    val progress: Float = 0f,
    val uploadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speed: Long = 0L,
    val averageSpeed: Long = 0L,
    val eta: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val lastError: String? = null,
    val retryCount: Int = 0,
    // Metadata for UI
    val thumbnailPath: String? = null,
    val duration: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val scheduledAt: Long? = null,
    val uploadDurationMs: Long = 0L,
    val messageLink: String? = null
)
