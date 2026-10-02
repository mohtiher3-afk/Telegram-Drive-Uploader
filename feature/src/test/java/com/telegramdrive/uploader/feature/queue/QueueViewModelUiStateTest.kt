package com.telegramdrive.uploader.feature.queue

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.core.util.OwnedStagedFileStore
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QueueViewModelUiStateTest {

    private val testDispatcher = StandardTestDispatcher()
    private val uploadsFlow = MutableStateFlow<List<UploadTask>>(emptyList())

    private val pausedIds = mutableListOf<String>()
    private val resumedTasks = mutableListOf<UploadTask>()
    private val cancelledIds = mutableListOf<String>()
    private val retriedTasks = mutableListOf<UploadTask>()

    private val fakeUploadRepository = object : UploadRepository {
        override fun getAllUploads(): Flow<List<UploadTask>> = uploadsFlow
        override fun getActiveUploads(): Flow<List<UploadTask>> = emptyFlow()
        override suspend fun getUploadById(id: String): UploadTask? = uploadsFlow.value.find { it.id == id }
        override fun observeUploadById(id: String): Flow<UploadTask?> = emptyFlow()
        override suspend fun insertUpload(upload: UploadTask) {}
        override suspend fun updateStatus(id: String, status: UploadStatus) {}
        override suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>): Boolean = true
        override suspend fun bumpExecutionGeneration(id: String, allowedStatuses: List<UploadStatus>): Boolean = true
        override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long) {}
        override suspend fun updateProgressIfGeneration(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long, generation: Long): Boolean = true
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {}
        override suspend fun updateMessageLink(id: String, messageLink: String) {}
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {}
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) {}
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() {}
    }

    private val fakeUploadManager = object : UploadManager {
        override fun enqueueUpload(task: UploadTask) {}
        override fun pauseUpload(id: String) {
            pausedIds.add(id)
        }
        override fun resumeUpload(task: UploadTask) {
            resumedTasks.add(task)
        }
        override fun cancelUpload(id: String) {
            cancelledIds.add(id)
        }
        override fun retryUpload(task: UploadTask) {
            retriedTasks.add(task)
        }
        override fun observeUpload(id: String): Flow<UploadTask?> = emptyFlow()
        override fun observeUploads(): Flow<List<UploadTask>> = emptyFlow()
    }

    private lateinit var stagedFileStore: OwnedStagedFileStore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        stagedFileStore = OwnedStagedFileStore(context)
        pausedIds.clear()
        resumedTasks.clear()
        cancelledIds.clear()
        retriedTasks.clear()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testTask(id: String, status: UploadStatus, name: String = "file_$id.mp4"): UploadTask = UploadTask(
        id = id,
        sourceUri = "content://media/$id",
        fileName = name,
        fileSize = 1000L,
        totalBytes = 1000L,
        status = status
    )

    @Test
    fun filteringAndSearchingUpdatesUiState() = runTest(testDispatcher) {
        val viewModel = QueueViewModel(fakeUploadRepository, fakeUploadManager, stagedFileStore)
        val collectJob = launch { viewModel.uiState.collect {} }

        val tasks = listOf(
            testTask("t1", UploadStatus.QUEUED, "alpha.mp4"),
            testTask("t2", UploadStatus.UPLOADING, "beta.mp4"),
            testTask("t3", UploadStatus.PAUSED, "gamma.mp4"),
            testTask("t4", UploadStatus.FAILED, "delta.mp4"),
            testTask("t5", UploadStatus.COMPLETED, "omega.mp4")
        )
        uploadsFlow.value = tasks
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(4, state.queueItems.size)
        assertEquals(1, state.failedCount)
        assertEquals(1, state.pausedCount)
        assertEquals(2, state.activeCount)

        viewModel.selectFilter(QueueFilter.ACTIVE)
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(2, state.queueItems.size)
        assertTrue(state.queueItems.all { it.id == "t1" || it.id == "t2" })

        viewModel.selectFilter(QueueFilter.PAUSED)
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(1, state.queueItems.size)
        assertEquals("t3", state.queueItems[0].id)

        viewModel.selectFilter(QueueFilter.ALL)
        viewModel.onQueryChanged("beta")
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(1, state.queueItems.size)
        assertEquals("t2", state.queueItems[0].id)

        collectJob.cancel()
    }

    @Test
    fun actionsTriggerManagerAndRepository() = runTest(testDispatcher) {
        val viewModel = QueueViewModel(fakeUploadRepository, fakeUploadManager, stagedFileStore)
        val collectJob = launch { viewModel.uiState.collect {} }

        val taskPaused = testTask("p1", UploadStatus.PAUSED)
        val taskActive = testTask("a1", UploadStatus.UPLOADING)
        val taskFailed = testTask("f1", UploadStatus.FAILED)
        uploadsFlow.value = listOf(taskPaused, taskActive, taskFailed)
        advanceUntilIdle()

        viewModel.pauseUpload("a1")
        advanceUntilIdle()
        assertTrue("pauseUpload should forward to manager", pausedIds.contains("a1"))

        viewModel.resumeUpload("p1")
        advanceUntilIdle()
        assertTrue("resumeUpload should forward to manager", resumedTasks.any { it.id == "p1" })

        viewModel.retryUpload("f1")
        advanceUntilIdle()
        assertTrue("retryUpload should forward to manager", retriedTasks.any { it.id == "f1" })

        viewModel.cancelUpload("a1")
        advanceUntilIdle()
        assertTrue("cancelUpload should forward to manager", cancelledIds.contains("a1"))

        collectJob.cancel()
    }
}
