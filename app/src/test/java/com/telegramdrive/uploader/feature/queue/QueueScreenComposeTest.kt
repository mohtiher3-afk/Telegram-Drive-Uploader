package com.telegramdrive.uploader.feature.queue

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QueueScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `empty queue shows full screen empty state`() {
        composeRule.setContent {
            QueueScreen(viewModel = QueueViewModel(FakeQueueRepository(), FakeUploadManager()))
        }

        composeRule.onNodeWithTag("queue_empty_state").assertIsDisplayed()
    }

    @Test
    fun `mixed queue shows search field and all filter chips`() {
        setContent(
            tasks = listOf(
                task("1", UploadStatus.UPLOADING),
                task("2", UploadStatus.PAUSED),
                task("3", UploadStatus.FAILED)
            )
        )

        composeRule.onNodeWithTag("queue_search_field").assertIsDisplayed()
        QueueFilter.values().forEach { filter ->
            composeRule.onNodeWithTag("queue_filter_${filter.name.lowercase()}").assertIsDisplayed()
        }
    }

    @Test
    fun `typing a query filters the queue list`() {
        setContent(
            tasks = listOf(
                task("1", UploadStatus.UPLOADING, name = "vacation_clip.mp4"),
                task("2", UploadStatus.FAILED, name = "meeting_recording.mp4")
            )
        )

        composeRule.onNodeWithTag("queue_search_field").performTextInput("meeting")

        composeRule.waitForIdle()
        composeRule.onNode(hasText("meeting_recording.mp4")).assertIsDisplayed()
        composeRule.onNode(hasText("vacation_clip.mp4")).assertDoesNotExist()
    }

    @Test
    fun `non matching query shows no results message`() {
        setContent(tasks = listOf(task("1", UploadStatus.UPLOADING)))

        composeRule.onNodeWithTag("queue_search_field").performTextInput("zzz_none")

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("queue_list")
            .performScrollToNode(hasTestTag("queue_no_results"))
        composeRule.onNodeWithTag("queue_no_results").assertIsDisplayed()
    }

    @Test
    fun `failed filter shows only failed items and retry control`() {
        setContent(
            tasks = listOf(
                task("1", UploadStatus.UPLOADING, name = "active_video.mp4"),
                task("2", UploadStatus.FAILED, name = "broken_video.mp4"),
                task("3", UploadStatus.PAUSED, name = "paused_video.mp4")
            )
        )

        composeRule.onNodeWithTag("queue_filter_failed").performClick()

        composeRule.waitForIdle()
        composeRule.onNode(hasText("broken_video.mp4")).assertIsDisplayed()
        composeRule.onNode(hasText("active_video.mp4")).assertDoesNotExist()
        composeRule.onNode(hasText("paused_video.mp4")).assertDoesNotExist()
        composeRule.onNodeWithTag("retry_all_failed").performScrollTo().assertExists()
    }

    @Test
    fun `failed count is reflected in the top bar summary`() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        setContent(tasks = listOf(task("1", UploadStatus.FAILED)))

        composeRule.waitForIdle()
        val summary = ctx.getString(
            com.telegramdrive.uploader.R.string.queue_count_summary,
            0,
            1
        )
        composeRule.onNode(hasText(summary, substring = true)).assertExists()
    }

    @Test
    fun `pause all active pauses only queued and in-flight tasks`() {
        val manager = FakeUploadManager()
        setContent(
            tasks = listOf(
                task("1", UploadStatus.UPLOADING),
                task("2", UploadStatus.QUEUED),
                task("3", UploadStatus.PAUSED),
                task("4", UploadStatus.FAILED)
            ),
            manager = manager
        )

        composeRule.onNodeWithTag("pause_all_active").performScrollTo().performClick()

        composeRule.waitForIdle()
        assertTrue(manager.paused.contains("1"))
        assertTrue(manager.paused.contains("2"))
        assertFalse(manager.paused.contains("3"))
        assertFalse(manager.paused.contains("4"))
    }

    @Test
    fun `retry all failed retries only failed tasks`() {
        val manager = FakeUploadManager()
        setContent(
            tasks = listOf(
                task("1", UploadStatus.UPLOADING, name = "active_video.mp4"),
                task("2", UploadStatus.FAILED, name = "broken_one.mp4"),
                task("3", UploadStatus.FAILED, name = "broken_two.mp4")
            ),
            manager = manager
        )

        composeRule.onNodeWithTag("retry_all_failed").performScrollTo().performClick()

        composeRule.waitForIdle()
        assertEquals(listOf("2", "3"), manager.retried.map { it.id })
    }

    private fun setContent(
        tasks: List<UploadTask>,
        manager: FakeUploadManager = FakeUploadManager()
    ) {
        composeRule.setContent {
            QueueScreen(viewModel = QueueViewModel(FakeQueueRepository(tasks), manager))
        }
        composeRule.waitForIdle()
    }

    private fun task(
        id: String,
        status: UploadStatus,
        name: String = "video_$id.mp4"
    ): UploadTask = UploadTask(
        id = id,
        sourceUri = "content://media/$id",
        fileName = name,
        fileSize = 2048L,
        totalBytes = 2048L,
        status = status
    )

    private class FakeQueueRepository(initial: List<UploadTask> = emptyList()) : UploadRepository {
        private val tasks = MutableStateFlow(initial)

        override fun getAllUploads(): Flow<List<UploadTask>> = tasks

        override fun getActiveUploads(): Flow<List<UploadTask>> = tasks.map { list ->
            list.filter { it.status in ACTIVE_STATUSES }
        }

        override suspend fun getUploadById(id: String): UploadTask? =
            tasks.value.firstOrNull { it.id == id }

        override fun observeUploadById(id: String): Flow<UploadTask?> =
            tasks.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun insertUpload(upload: UploadTask) {
            tasks.value = tasks.value + upload
        }

        override suspend fun updateStatus(id: String, status: UploadStatus) {
            tasks.value = tasks.value.map { if (it.id == id) it.copy(status = status) else it }
        }

        override suspend fun updateStatusIf(
            id: String,
            status: UploadStatus,
            allowedStatuses: List<UploadStatus>
        ) {
            val current = tasks.value.firstOrNull { it.id == id }?.status
            if (current in allowedStatuses) updateStatus(id, status)
        }

        override suspend fun updateProgress(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long
        ) {
            tasks.value = tasks.value.map {
                if (it.id == id) {
                    it.copy(
                        uploadedBytes = uploadedBytes,
                        totalBytes = totalBytes,
                        progress = progress,
                        speed = speed,
                        averageSpeed = averageSpeed,
                        eta = eta
                    )
                } else it
            }
        }

        override suspend fun updateUploadDuration(id: String, durationMs: Long) {
            tasks.value = tasks.value.map {
                if (it.id == id) it.copy(uploadDurationMs = durationMs) else it
            }
        }

        override suspend fun updateMessageLink(id: String, messageLink: String) {
            tasks.value = tasks.value.map {
                if (it.id == id) it.copy(messageLink = messageLink) else it
            }
        }

        override suspend fun reconcileInterruptedUploads(): Int = 0

        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()

        override suspend fun deleteUploadById(id: String) {
            tasks.value = tasks.value.filterNot { it.id == id }
        }

        override suspend fun deleteCompletedUploads() {
            tasks.value = tasks.value.filterNot { it.status == UploadStatus.COMPLETED }
        }

        override suspend fun clearAllUploads() {
            tasks.value = emptyList()
        }
    }

    private class FakeUploadManager : UploadManager {
        val paused = mutableListOf<String>()
        val resumed = mutableListOf<UploadTask>()
        val cancelled = mutableListOf<String>()
        val retried = mutableListOf<UploadTask>()

        override fun enqueueUpload(task: UploadTask) {}

        override fun pauseUpload(id: String) {
            paused += id
        }

        override fun resumeUpload(task: UploadTask) {
            resumed += task
        }

        override fun cancelUpload(id: String) {
            cancelled += id
        }

        override fun retryUpload(task: UploadTask) {
            retried += task
        }

        override fun observeUpload(id: String): Flow<UploadTask?> = flowOf(null)

        override fun observeUploads(): Flow<List<UploadTask>> = flowOf(emptyList())
    }

    private companion object {
        val ACTIVE_STATUSES = setOf(
            UploadStatus.QUEUED,
            UploadStatus.PREPARING,
            UploadStatus.UPLOADING,
            UploadStatus.RETRYING
        )
    }
}