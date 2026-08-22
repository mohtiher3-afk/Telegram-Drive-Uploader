package com.telegramdrive.uploader.data.repository

import com.telegramdrive.uploader.data.local.UploadDao
import com.telegramdrive.uploader.data.local.UploadEntity
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepositoryImpl @Inject constructor(
    private val uploadDao: UploadDao
) : UploadRepository {
    override fun getAllUploads(): Flow<List<UploadTask>> {
        return uploadDao.getAllUploads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveUploads(): Flow<List<UploadTask>> {
        return uploadDao.getActiveUploads().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getUploadById(id: String): UploadTask? {
        return uploadDao.getUploadById(id)?.toDomain()
    }

    override fun observeUploadById(id: String): Flow<UploadTask?> {
        return uploadDao.observeUploadById(id).map { it?.toDomain() }
    }

    override suspend fun insertUpload(upload: UploadTask) {
        uploadDao.insertUpload(upload.toEntity())
    }

    override suspend fun updateStatus(id: String, status: UploadStatus) {
        uploadDao.updateStatus(id, status.name)
    }

    override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long) {
        uploadDao.updateProgress(id, uploadedBytes, totalBytes, progress, speed, averageSpeed, eta)
    }

    override suspend fun updateUploadDuration(id: String, durationMs: Long) {
        uploadDao.updateUploadDuration(id, durationMs.coerceAtLeast(0L))
    }

    override suspend fun deleteUploadById(id: String) {
        uploadDao.deleteUploadById(id)
    }

    override suspend fun deleteCompletedUploads() {
        uploadDao.deleteCompletedUploads()
    }

    override suspend fun clearAllUploads() {
        uploadDao.clearAllUploads()
    }

    private fun UploadEntity.toDomain(): UploadTask {
        return UploadTask(
            id = id,
            sourceUri = sourceUri,
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType,
            destinationId = destinationId,
            status = UploadStatus.valueOf(status),
            progress = progress,
            uploadedBytes = uploadedBytes,
            totalBytes = totalBytes,
            speed = speed,
            averageSpeed = averageSpeed,
            eta = eta,
            createdAt = createdAt,
            startedAt = startedAt,
            completedAt = completedAt,
            lastError = lastError,
            retryCount = retryCount,
            thumbnailPath = thumbnailPath,
            duration = duration,
                        width = width,
            height = height,
            scheduledAt = scheduledAt,
            uploadDurationMs = uploadDurationMs
        )
    }
    private fun UploadTask.toEntity(): UploadEntity {
        return UploadEntity(
            id = id,
            sourceUri = sourceUri,
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType,
            destinationId = destinationId,
            status = status.name,
            progress = progress,
            uploadedBytes = uploadedBytes,
            totalBytes = totalBytes,
            speed = speed,
            averageSpeed = averageSpeed,
            eta = eta,
            createdAt = createdAt,
            startedAt = startedAt,
            completedAt = completedAt,
            lastError = lastError,
            retryCount = retryCount,
            thumbnailPath = thumbnailPath,
            duration = duration,
            width = width,
            height = height,
            scheduledAt = scheduledAt,
            uploadDurationMs = uploadDurationMs
        )
    }
}
