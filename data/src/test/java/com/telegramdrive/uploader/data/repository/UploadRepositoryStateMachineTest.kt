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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UploadRepositoryStateMachineTest {

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

    private fun testEntity(
        id: String,
        status: String = "QUEUED",
        generation: Long = 0L,
        progress: Float = 0f,
        uploadedBytes: Long = 0L,
        totalBytes: Long = 10_000L
    ): UploadEntity = UploadEntity(
        id = id,
        sourceUri = "content://media/$id",
        fileName = "$id.mp4",
        fileSize = totalBytes,
        mimeType = "video/mp4",
        destinationId = 12345L,
        status = status,
        progress = progress,
        uploadedBytes = uploadedBytes,
        totalBytes = totalBytes,
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
        uploadDurationMs = 0L,
        messageLink = null,
        provisionalMessageId = null,
        sendDispatched = false,
        finalMessageId = null,
        executionGeneration = generation
    )

    @Test
    fun updateStatusIf_transitionsWhenPredecessorMatches() = runBlocking {
        uploadDao.insertUpload(testEntity("upload-1", status = "QUEUED"))

        val transitioned = repository.updateStatusIf(
            id = "upload-1",
            status = UploadStatus.PREPARING,
            allowedStatuses = listOf(UploadStatus.QUEUED)
        )

        assertTrue("Expected successful transition from QUEUED to PREPARING", transitioned)
        val loaded = repository.getUploadById("upload-1")
        assertNotNull(loaded)
        assertEquals(UploadStatus.PREPARING, loaded?.status)
    }

    @Test
    fun updateStatusIf_rejectsWhenPredecessorDoesNotMatch() = runBlocking {
        uploadDao.insertUpload(testEntity("upload-2", status = "FAILED"))

        val transitioned = repository.updateStatusIf(
            id = "upload-2",
            status = UploadStatus.PAUSED,
            allowedStatuses = listOf(UploadStatus.QUEUED, UploadStatus.PREPARING, UploadStatus.UPLOADING)
        )

        assertFalse("Expected rejected transition from FAILED to PAUSED", transitioned)
        val loaded = repository.getUploadById("upload-2")
        assertEquals(UploadStatus.FAILED, loaded?.status)
    }

    @Test
    fun bumpExecutionGeneration_incrementsGenerationOnlyForAllowedStatus() = runBlocking {
        uploadDao.insertUpload(testEntity("upload-3", status = "PAUSED", generation = 0L))

        val bumped = repository.bumpExecutionGeneration(
            id = "upload-3",
            allowedStatuses = listOf(UploadStatus.PAUSED)
        )

        assertTrue("Expected bumpExecutionGeneration to succeed on PAUSED entity", bumped)
        val loaded = repository.getUploadById("upload-3")
        assertEquals(1L, loaded?.executionGeneration)

        val rejectedBump = repository.bumpExecutionGeneration(
            id = "upload-3",
            allowedStatuses = listOf(UploadStatus.QUEUED)
        )
        assertFalse("Expected bumpExecutionGeneration to be rejected when status not allowed", rejectedBump)
        assertEquals(1L, repository.getUploadById("upload-3")?.executionGeneration)
    }

    @Test
    fun updateProgressIfGeneration_updatesOnlyMatchingGenerationAndActiveStatus() = runBlocking {
        uploadDao.insertUpload(testEntity("upload-4", status = "UPLOADING", generation = 2L, totalBytes = 10_000L))

        val staleUpdate = repository.updateProgressIfGeneration(
            id = "upload-4",
            uploadedBytes = 5000L,
            totalBytes = 10_000L,
            progress = 0.5f,
            speed = 1000L,
            averageSpeed = 1000L,
            eta = 5L,
            generation = 1L
        )
        assertFalse("Stale generation update must be rejected", staleUpdate)
        var loaded = repository.getUploadById("upload-4")
        assertEquals(0f, loaded?.progress ?: -1f, 0.001f)
        assertEquals(0L, loaded?.uploadedBytes)

        val validUpdate = repository.updateProgressIfGeneration(
            id = "upload-4",
            uploadedBytes = 5000L,
            totalBytes = 10_000L,
            progress = 0.5f,
            speed = 1000L,
            averageSpeed = 1000L,
            eta = 5L,
            generation = 2L
        )
        assertTrue("Matching generation update must succeed", validUpdate)
        loaded = repository.getUploadById("upload-4")
        assertEquals(0.5f, loaded?.progress ?: -1f, 0.001f)
        assertEquals(5000L, loaded?.uploadedBytes)
        assertEquals(UploadStatus.UPLOADING, loaded?.status)
    }

    @Test
    fun reconcileInterruptedUploads_resetsInFlightUploadsToQueued() = runBlocking {
        uploadDao.insertUpload(testEntity("upload-prep", status = "PREPARING"))
        uploadDao.insertUpload(testEntity("upload-prog", status = "UPLOADING"))
        uploadDao.insertUpload(testEntity("upload-paused", status = "PAUSED"))
        uploadDao.insertUpload(testEntity("upload-comp", status = "COMPLETED"))

        val count = repository.reconcileInterruptedUploads()

        assertEquals("Expected exactly 2 interrupted in-flight records to be reconciled", 2, count)
        assertEquals(UploadStatus.QUEUED, repository.getUploadById("upload-prep")?.status)
        assertEquals(UploadStatus.QUEUED, repository.getUploadById("upload-prog")?.status)
        assertEquals(UploadStatus.PAUSED, repository.getUploadById("upload-paused")?.status)
        assertEquals(UploadStatus.COMPLETED, repository.getUploadById("upload-comp")?.status)
    }

    @Test
    fun getActiveUploads_includesOnlyUnfinishedActiveStatuses() = runBlocking {
        uploadDao.insertUpload(testEntity("task-queued", status = "QUEUED"))
        uploadDao.insertUpload(testEntity("task-prep", status = "PREPARING"))
        uploadDao.insertUpload(testEntity("task-up", status = "UPLOADING"))
        uploadDao.insertUpload(testEntity("task-retry", status = "RETRYING"))
        uploadDao.insertUpload(testEntity("task-paused", status = "PAUSED"))
        uploadDao.insertUpload(testEntity("task-failed", status = "FAILED"))
        uploadDao.insertUpload(testEntity("task-completed", status = "COMPLETED"))

        val activeList = repository.getActiveUploads().first()
        val activeIds = activeList.map { it.id }.toSet()

        assertEquals(4, activeList.size)
        assertTrue(activeIds.contains("task-queued"))
        assertTrue(activeIds.contains("task-prep"))
        assertTrue(activeIds.contains("task-up"))
        assertTrue(activeIds.contains("task-retry"))
        assertFalse(activeIds.contains("task-paused"))
        assertFalse(activeIds.contains("task-failed"))
        assertFalse(activeIds.contains("task-completed"))
    }

    @Test
    fun deleteCompletedUploads_preservesNonCompletedTasks() = runBlocking {
        uploadDao.insertUpload(testEntity("keep-1", status = "QUEUED"))
        uploadDao.insertUpload(testEntity("keep-2", status = "FAILED"))
        uploadDao.insertUpload(testEntity("drop-1", status = "COMPLETED"))
        uploadDao.insertUpload(testEntity("drop-2", status = "COMPLETED"))

        repository.deleteCompletedUploads()

        val remaining = repository.getAllUploads().first().map { it.id }
        assertEquals(2, remaining.size)
        assertTrue(remaining.contains("keep-1"))
        assertTrue(remaining.contains("keep-2"))
        assertFalse(remaining.contains("drop-1"))
        assertFalse(remaining.contains("drop-2"))
    }
}
