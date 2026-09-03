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
    val messageLink: String? = null
)
