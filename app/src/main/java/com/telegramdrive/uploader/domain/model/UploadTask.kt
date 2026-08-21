package com.telegramdrive.uploader.domain.model

data class UploadTask(
    val id: String,
    val sourceUri: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val destinationId: Long,
    val status: UploadStatus,
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
    // Metadata for UI
    val thumbnailPath: String?,
    val duration: Long,
    val width: Int,
    val height: Int,
    val scheduledAt: Long? = null
)
