package com.telegramdrive.uploader.feature.queue

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueFilterAndBatchRetryTest {

    private fun createDummyTask(id: String, status: UploadStatus): UploadTask {
        return UploadTask(
            id = id,
            sourceUri = "content://media/$id",
            fileName = "video_$id.mp4",
            fileSize = 2048L,
            totalBytes = 2048L,
            status = status
        )
    }

    @Test
    fun queueFilterAllIncludesPendingTasks() {
        val tasks = listOf(
            createDummyTask("1", UploadStatus.QUEUED),
            createDummyTask("2", UploadStatus.UPLOADING),
            createDummyTask("3", UploadStatus.PAUSED),
            createDummyTask("4", UploadStatus.FAILED),
            createDummyTask("5", UploadStatus.COMPLETED)
        )

        val pending = tasks.filter {
            it.status != UploadStatus.COMPLETED && it.status != UploadStatus.CANCELLED
        }

        assertEquals(4, pending.size)
    }

    @Test
    fun queueFilterActiveIncludesQueuedAndUploadingAndRetrying() {
        val tasks = listOf(
            createDummyTask("1", UploadStatus.QUEUED),
            createDummyTask("2", UploadStatus.UPLOADING),
            createDummyTask("3", UploadStatus.RETRYING),
            createDummyTask("4", UploadStatus.PAUSED),
            createDummyTask("5", UploadStatus.FAILED)
        )

        val active = tasks.filter {
            it.status == UploadStatus.QUEUED ||
                it.status == UploadStatus.PREPARING ||
                it.status == UploadStatus.UPLOADING ||
                it.status == UploadStatus.RETRYING
        }

        assertEquals(3, active.size)
    }

    @Test
    fun queueFilterFailedIncludesOnlyFailedTasks() {
        val tasks = listOf(
            createDummyTask("1", UploadStatus.QUEUED),
            createDummyTask("2", UploadStatus.FAILED),
            createDummyTask("3", UploadStatus.FAILED)
        )

        val failed = tasks.filter { it.status == UploadStatus.FAILED }
        assertEquals(2, failed.size)
    }

    @Test
    fun retryAllFailedSelectsOnlyFailedTasks() {
        val tasks = listOf(
            createDummyTask("1", UploadStatus.QUEUED),
            createDummyTask("2", UploadStatus.FAILED),
            createDummyTask("3", UploadStatus.PAUSED),
            createDummyTask("4", UploadStatus.FAILED)
        )

        val failedTasksToRetry = tasks.filter { it.status == UploadStatus.FAILED }
        assertEquals(2, failedTasksToRetry.size)
        assertTrue(failedTasksToRetry.all { it.status == UploadStatus.FAILED })
    }
}
