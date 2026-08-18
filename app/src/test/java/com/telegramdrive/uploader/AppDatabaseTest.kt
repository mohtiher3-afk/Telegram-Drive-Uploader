package com.telegramdrive.uploader

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.AppDatabase
import com.telegramdrive.uploader.data.local.UploadDao
import com.telegramdrive.uploader.data.local.UploadEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var uploadDao: UploadDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        uploadDao = db.uploadDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeUploadAndReadInList() = runBlocking {
        val entity = createUploadEntity("1", "QUEUED", 1000L)
        uploadDao.insertUpload(entity)

        val allUploads = uploadDao.getAllUploads().first()
        assertEquals(1, allUploads.size)
        assertEquals("1", allUploads[0].id)
        assertEquals("QUEUED", allUploads[0].status)
    }

    @Test
    fun testUpdateProgressAndObserve() = runBlocking {
        val entity = createUploadEntity("2", "QUEUED", 2000L)
        uploadDao.insertUpload(entity)

        uploadDao.updateProgress(
            id = "2",
            uploadedBytes = 500L,
            totalBytes = 2000L,
            progress = 0.25f,
            speed = 100L,
            averageSpeed = 100L,
            eta = 15L
        )

        val updated = uploadDao.getUploadById("2")
        assertNotNull(updated)
        assertEquals("UPLOADING", updated?.status)
        assertEquals(500L, updated?.uploadedBytes)
        assertEquals(2000L, updated?.totalBytes)
        assertEquals(0.25f, updated?.progress ?: 0f, 0.01f)
        assertEquals(100L, updated?.speed)
        assertEquals(15L, updated?.eta)
    }

    @Test
    fun testActiveUploadsFiltering() = runBlocking {
        val q1 = createUploadEntity("1", "QUEUED", 100L)
        val c1 = createUploadEntity("2", "COMPLETED", 100L)
        val f1 = createUploadEntity("3", "FAILED", 100L)
        val u1 = createUploadEntity("4", "UPLOADING", 100L)

        uploadDao.insertUploads(listOf(q1, c1, f1, u1))

        val active = uploadDao.getActiveUploads().first()
        // QUEUED and UPLOADING are in the active list (status list: 'QUEUED', 'PREPARING', 'UPLOADING', 'RETRYING')
        assertEquals(2, active.size)
        assertTrue(active.any { it.id == "1" })
        assertTrue(active.any { it.id == "4" })
    }

    @Test
    fun testDeleteCompletedUploads() = runBlocking {
        val q1 = createUploadEntity("1", "QUEUED", 100L)
        val c1 = createUploadEntity("2", "COMPLETED", 100L)
        
        uploadDao.insertUploads(listOf(q1, c1))

        var all = uploadDao.getAllUploads().first()
        assertEquals(2, all.size)

        uploadDao.deleteCompletedUploads()

        all = uploadDao.getAllUploads().first()
        assertEquals(1, all.size)
        assertEquals("1", all[0].id)
    }

    @Test
    fun testClearAllUploads() = runBlocking {
        val q1 = createUploadEntity("1", "QUEUED", 100L)
        val q2 = createUploadEntity("2", "QUEUED", 100L)

        uploadDao.insertUploads(listOf(q1, q2))
        assertEquals(2, uploadDao.getAllUploads().first().size)

        uploadDao.clearAllUploads()
        assertEquals(0, uploadDao.getAllUploads().first().size)
    }

    private fun createUploadEntity(id: String, status: String, createdAt: Long): UploadEntity {
        return UploadEntity(
            id = id,
            sourceUri = "content://media/external/video/media/$id",
            fileName = "video_$id.mp4",
            fileSize = 1024 * 1024 * 10L,
            mimeType = "video/mp4",
            destinationId = 987654321L,
            status = status,
            progress = 0f,
            uploadedBytes = 0L,
            totalBytes = 1024 * 1024 * 10L,
            speed = 0L,
            averageSpeed = 0L,
            eta = 0L,
            createdAt = createdAt,
            startedAt = null,
            completedAt = null,
            lastError = null,
            retryCount = 0,
            thumbnailPath = null,
            duration = 30L,
            width = 1280,
            height = 720
        )
    }
}
