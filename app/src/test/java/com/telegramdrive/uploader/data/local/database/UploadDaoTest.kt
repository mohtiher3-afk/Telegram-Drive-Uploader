package com.telegramdrive.uploader.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UploadDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var uploadDao: UploadDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        uploadDao = database.uploadDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestUpload(id: String, status: String): UploadEntity {
        return UploadEntity(
            id = id,
            sourceUri = "content://media/external/files/$id",
            fileName = "test_file_$id.mp4",
            fileSize = 1024L * 1024L,
            mimeType = "video/mp4",
            destinationId = 987654321L,
            status = status,
            progress = 0.0f,
            uploadedBytes = 0L,
            totalBytes = 1024L * 1024L,
            speed = 0L,
            averageSpeed = 0L,
            eta = 0L,
            createdAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            lastError = null,
            retryCount = 0,
            thumbnailPath = null,
            duration = 0L,
            width = 0,
            height = 0,
            scheduledAt = null,
            uploadDurationMs = 0L
        )
    }

    @Test
    fun insertAndGetUploadById() = runBlocking {
        val upload = createTestUpload("upload_1", "QUEUED")
        uploadDao.insertUpload(upload)

        val retrieved = uploadDao.getUploadById("upload_1")
        assertNotNull(retrieved)
        assertEquals("upload_1", retrieved?.id)
        assertEquals("QUEUED", retrieved?.status)
    }

    @Test
    fun updateStatus() = runBlocking {
        val upload = createTestUpload("upload_2", "QUEUED")
        uploadDao.insertUpload(upload)

        uploadDao.updateStatus("upload_2", "COMPLETED")

        val retrieved = uploadDao.getUploadById("upload_2")
        assertEquals("COMPLETED", retrieved?.status)
    }

    @Test
    fun getActiveUploads() = runBlocking {
        val active1 = createTestUpload("active_1", "PREPARING")
        val active2 = createTestUpload("active_2", "UPLOADING")
        val completed = createTestUpload("completed_1", "COMPLETED")

        uploadDao.insertUploads(listOf(active1, active2, completed))

        val activeList = uploadDao.getActiveUploads().first()
        assertEquals(2, activeList.size)
        assertTrue(activeList.any { it.id == "active_1" })
        assertTrue(activeList.any { it.id == "active_2" })
    }

    @Test
    fun reconcileInterruptedUploads() = runBlocking {
        val active1 = createTestUpload("active_1", "PREPARING")
        val active2 = createTestUpload("active_2", "UPLOADING")
        val completed = createTestUpload("completed_1", "COMPLETED")

        uploadDao.insertUploads(listOf(active1, active2, completed))

        val rowsAffected = uploadDao.reconcileInterruptedUploads()
        assertEquals(2, rowsAffected)

        val reconciled1 = uploadDao.getUploadById("active_1")
        val reconciled2 = uploadDao.getUploadById("active_2")
        val finalCompleted = uploadDao.getUploadById("completed_1")

        assertEquals("QUEUED", reconciled1?.status)
        assertEquals("QUEUED", reconciled2?.status)
        assertEquals("COMPLETED", finalCompleted?.status)
    }

    @Test
    fun deleteUploadById() = runBlocking {
        val upload = createTestUpload("upload_3", "QUEUED")
        uploadDao.insertUpload(upload)

        uploadDao.deleteUploadById("upload_3")
        val retrieved = uploadDao.getUploadById("upload_3")
        assertNull(retrieved)
    }

    @Test
    fun clearAllUploads() = runBlocking {
        val upload1 = createTestUpload("upload_4", "QUEUED")
        val upload2 = createTestUpload("upload_5", "COMPLETED")
        uploadDao.insertUploads(listOf(upload1, upload2))

        uploadDao.clearAllUploads()

        val allUploads = uploadDao.getAllUploads().first()
        assertTrue(allUploads.isEmpty())
    }
}
