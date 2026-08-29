package com.telegramdrive.uploader.feature.history

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryFilterAndSortingTest {

    private fun createCompletedTask(id: String, name: String, size: Long, time: Long): UploadTask {
        return UploadTask(
            id = id,
            sourceUri = "content://media/$id",
            fileName = name,
            fileSize = size,
            totalBytes = size,
            status = UploadStatus.COMPLETED,
            completedAt = time
        )
    }

    @Test
    fun historyFiltersCompletedOnly() {
        val tasks = listOf(
            createCompletedTask("1", "clip1.mp4", 1000L, 5000L),
            UploadTask(
                id = "2",
                sourceUri = "content://2",
                fileName = "active.mp4",
                fileSize = 1000L,
                totalBytes = 1000L,
                status = UploadStatus.UPLOADING
            )
        )

        val completed = tasks.filter { it.status == UploadStatus.COMPLETED }
        assertEquals(1, completed.size)
        assertEquals("clip1.mp4", completed[0].fileName)
    }

    @Test
    fun historyFiltersBySearchQuery() {
        val tasks = listOf(
            createCompletedTask("1", "holiday_video.mp4", 1000L, 5000L),
            createCompletedTask("2", "work_presentation.mp4", 2000L, 6000L),
            createCompletedTask("3", "holiday_photo.mp4", 500L, 7000L)
        )

        val query = "holiday"
        val filtered = tasks.filter { it.fileName.lowercase().contains(query) }

        assertEquals(2, filtered.size)
    }

    @Test
    fun historySortsByNewestAndLargest() {
        val tasks = listOf(
            createCompletedTask("1", "old_small.mp4", 1000L, 1000L),
            createCompletedTask("2", "new_large.mp4", 5000L, 3000L),
            createCompletedTask("3", "mid_mid.mp4", 3000L, 2000L)
        )

        val sortedByNewest = tasks.sortedByDescending { it.completedAt ?: it.createdAt }
        assertEquals("new_large.mp4", sortedByNewest.first().fileName)

        val sortedByLargest = tasks.sortedByDescending { it.fileSize }
        assertEquals("new_large.mp4", sortedByLargest.first().fileName)
    }
}
