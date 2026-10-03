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
    suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>): Boolean
    suspend fun bumpExecutionGeneration(id: String, allowedStatuses: List<UploadStatus>): Boolean = false
    suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long)
    suspend fun updateProgressIfGeneration(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long, generation: Long): Boolean = false
    suspend fun updateUploadDuration(id: String, durationMs: Long)
    suspend fun updateMessageLink(id: String, messageLink: String)
    suspend fun updateProvisionalMessageId(id: String, messageId: Long)
    suspend fun reconcileInterruptedUploads(): Int
    suspend fun getInterruptedUploads(): List<UploadTask>
    suspend fun deleteUploadById(id: String)
    /** Deletes a history item only when it is still terminal. */
    suspend fun deleteCompletedUploadById(id: String) {
        if (getUploadById(id)?.status == UploadStatus.COMPLETED) {
            deleteUploadById(id)
        }
    }
    suspend fun deleteCompletedUploads()
    suspend fun clearAllUploads()
}
