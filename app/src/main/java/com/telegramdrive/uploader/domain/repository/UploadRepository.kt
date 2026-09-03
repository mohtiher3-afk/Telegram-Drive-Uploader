package com.telegramdrive.uploader.domain.repository

import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import kotlinx.coroutines.flow.Flow

interface UploadRepository {
    fun getAllUploads(): Flow<List<UploadTask>>
    fun getActiveUploads(): Flow<List<UploadTask>>
    suspend fun getUploadById(id: String): UploadTask?
    fun observeUploadById(id: String): Flow<UploadTask?>
    suspend fun insertUpload(upload: UploadTask)
    suspend fun updateStatus(id: String, status: UploadStatus)
    suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>)
    suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long)
    suspend fun updateUploadDuration(id: String, durationMs: Long)
    suspend fun updateMessageLink(id: String, messageLink: String)
    suspend fun reconcileInterruptedUploads(): Int
    suspend fun getInterruptedUploads(): List<UploadTask>
    suspend fun deleteUploadById(id: String)
    suspend fun deleteCompletedUploads()
    suspend fun clearAllUploads()
}
