package com.telegramdrive.uploader.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.database.AppDatabase
import com.telegramdrive.uploader.data.local.database.UploadDao
import com.telegramdrive.uploader.data.local.database.UploadEntity
import com.telegramdrive.uploader.domain.model.UploadStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A raw status string from the database must never crash the Flow collectors that
 * feed home/queue/history: UploadStatus.valueOf used to throw
 * IllegalArgumentException for unknown values, wedging every collector.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UploadRepositoryStatusFallbackTest {

    private lateinit var database: AppDatabase
    private lateinit var uploadDao: UploadDao
    private lateinit var repository: UploadRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        uploadDao = database.uploadDao()
        repository = UploadRepositoryImpl(uploadDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(id: String, status: String): UploadEntity = UploadEntity(
        id = id,
        sourceUri = "file:///tmp/$id.mp4",
        fileName = "$id.mp4",
        fileSize = 1024L,
        mimeType = "video/mp4",
        destinationId = 5L,
        status = status,
        progress = 0f,
        uploadedBytes = 0L,
        totalBytes = 1024L,
        speed = 0L,
        averageSpeed = 0L,
        eta = 0L,
        createdAt = 0L,
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

    @Test
    fun unknownPersistedStatusMapsToFailedInsteadOfCrashing() = runBlocking {
        uploadDao.insertUpload(entity("broken-1", "NOT_A_REAL_STATUS"))
        uploadDao.insertUpload(entity("broken-2", ""))

        val tasks = repository.getAllUploads().first()

        assertEquals(2, tasks.size)
        tasks.forEach { task ->
            assertEquals(
                "Unknown persisted status must map to FAILED",
                UploadStatus.FAILED,
                task.status
            )
        }
    }

    @Test
    fun unknownStatusIsAlsoSafeThroughSingleTaskLookups() = runBlocking {
        uploadDao.insertUpload(entity("broken-1", "GARBAGE_STATUS"))

        assertEquals(UploadStatus.FAILED, repository.getUploadById("broken-1")?.status)
        assertEquals(UploadStatus.FAILED, repository.observeUploadById("broken-1").first()?.status)
    }

    @Test
    fun validStatusesStillPassThrough() = runBlocking {
        uploadDao.insertUpload(entity("ok-1", "QUEUED"))
        uploadDao.insertUpload(entity("ok-2", "COMPLETED"))
        uploadDao.insertUpload(entity("ok-3", "RETRYING"))

        val byId = repository.getAllUploads().first().associateBy { it.id }

        assertEquals(UploadStatus.QUEUED, byId["ok-1"]?.status)
        assertEquals(UploadStatus.COMPLETED, byId["ok-2"]?.status)
        assertEquals(UploadStatus.RETRYING, byId["ok-3"]?.status)
    }
}
