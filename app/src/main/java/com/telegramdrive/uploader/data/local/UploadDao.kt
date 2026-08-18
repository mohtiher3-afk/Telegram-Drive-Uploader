package com.telegramdrive.uploader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UploadDao {
    @Query("SELECT * FROM uploads ORDER BY createdAt DESC")
    fun getAllUploads(): Flow<List<UploadEntity>>

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

    @Query("UPDATE uploads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE uploads SET uploadedBytes = :uploadedBytes, totalBytes = :totalBytes, progress = :progress, speed = :speed, averageSpeed = :averageSpeed, eta = :eta, status = 'UPLOADING' WHERE id = :id")
    suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long)

    @Query("DELETE FROM uploads WHERE id = :id")
    suspend fun deleteUploadById(id: String)

    @Query("DELETE FROM uploads WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedUploads()

    @Query("DELETE FROM uploads")
    suspend fun clearAllUploads()
}
