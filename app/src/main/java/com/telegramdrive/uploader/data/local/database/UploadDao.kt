package com.telegramdrive.uploader.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Query("SELECT * FROM uploads ORDER BY createdAt DESC")
    fun getAllUploads(): Flow<List<UploadEntity>>

    @Query("SELECT * FROM uploads WHERE status IN ('PREPARING', 'UPLOADING')")
    suspend fun getInterruptedUploads(): List<UploadEntity>

    @Query("SELECT * FROM uploads WHERE status IN ('QUEUED', 'PREPARING', 'UPLOADING', 'RETRYING') ORDER BY createdAt ASC")
    fun getActiveUploads(): Flow<List<UploadEntity>>

    @Query("SELECT * FROM uploads WHERE id = :id")
    suspend fun getUploadById(id: String): UploadEntity?

    @Query("SELECT * FROM uploads WHERE id = :id")
    fun observeUploadById(id: String): Flow<UploadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpload(upload: UploadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploads(uploads: List<UploadEntity>)

    @Query("UPDATE uploads SET status = :status WHERE id = :id AND status IN (:allowedStatuses)")
    suspend fun updateStatusIf(id: String, status: String, allowedStatuses: List<String>): Int

    @Query("UPDATE uploads SET status = :status WHERE id = :id AND status NOT IN ('COMPLETED', 'FAILED', 'CANCELLED')")
    suspend fun updateStatus(id: String, status: String): Int

    @Query("UPDATE uploads SET uploadedBytes = :uploadedBytes, totalBytes = :totalBytes, progress = :progress, speed = :speed, averageSpeed = :averageSpeed, eta = :eta, status = 'UPLOADING' WHERE id = :id AND status IN ('PREPARING', 'UPLOADING')")
    suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long): Int

    @Query("UPDATE uploads SET uploadDurationMs = :durationMs WHERE id = :id")
    suspend fun updateUploadDuration(id: String, durationMs: Long)

    @Query("UPDATE uploads SET status = 'QUEUED' WHERE status IN ('PREPARING', 'UPLOADING')")
    suspend fun reconcileInterruptedUploads(): Int

    @Query("DELETE FROM uploads WHERE id = :id")
    suspend fun deleteUploadById(id: String)

    @Query("DELETE FROM uploads WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedUploads()

    @Query("DELETE FROM uploads")
    suspend fun clearAllUploads()
}
