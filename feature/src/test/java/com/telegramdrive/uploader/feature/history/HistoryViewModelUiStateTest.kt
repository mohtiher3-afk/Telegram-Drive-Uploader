package com.telegramdrive.uploader.feature.history

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelUiStateTest {

    private val testDispatcher = StandardTestDispatcher()
    private val uploadsFlow = MutableStateFlow<List<UploadTask>>(emptyList())
    private val deletedIds = mutableListOf<String>()
    private var completedCleared = false

    private val fakeRepo = object : UploadRepository {
        override fun getAllUploads(): Flow<List<UploadTask>> = uploadsFlow
        override fun getActiveUploads(): Flow<List<UploadTask>> = emptyFlow()
        override suspend fun getUploadById(id: String): UploadTask? = null
        override fun observeUploadById(id: String): Flow<UploadTask?> = emptyFlow()
        override suspend fun insertUpload(upload: UploadTask) {}
        override suspend fun updateStatus(id: String, status: UploadStatus) {}
        override suspend fun updateStatusIf(id: String, s: UploadStatus, a: List<UploadStatus>) = true
        override suspend fun bumpExecutionGeneration(id: String, a: List<UploadStatus>) = true
        override suspend fun updateProgress(id: String, u: Long, t: Long, p: Float, s: Long, a: Long, e: Long) {}
        override suspend fun updateProgressIfGeneration(id: String, u: Long, t: Long, p: Float, s: Long, a: Long, e: Long, g: Long) = true
        override suspend fun updateUploadDuration(id: String, d: Long) {}
        override suspend fun updateMessageLink(id: String, m: String) {}
        override suspend fun updateProvisionalMessageId(id: String, m: Long) {}
        override suspend fun reconcileInterruptedUploads() = 0
        override suspend fun getInterruptedUploads() = emptyList<UploadTask>()
        override suspend fun deleteUploadById(id: String) { deletedIds.add(id) }
        override suspend fun deleteCompletedUploads() { completedCleared = true }
        override suspend fun clearAllUploads() {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        deletedIds.clear()
        completedCleared = false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun task(id: String, name: String, size: Long, status: UploadStatus, time: Long?) = UploadTask(
        id = id, sourceUri = "content://$id", fileName = name, fileSize = size,
        totalBytes = size, status = status, completedAt = time
    )

    @Test
    fun uiStateFiltersCompletedAndCalculatesTotals() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        uploadsFlow.value = listOf(
            task("1", "doc1.pdf", 1000L, UploadStatus.COMPLETED, now - 1000),
            task("2", "vid.mp4", 4000L, UploadStatus.COMPLETED, now - 500),
            task("3", "pending.mp4", 5000L, UploadStatus.UPLOADING, null)
        )
        val viewModel = HistoryViewModel(fakeRepo)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalMatches)
        assertEquals(5000L, state.totalSize)
        assertEquals(listOf("vid.mp4", "doc1.pdf"), state.historyItems.map { it.fileName })
        job.cancel()
    }

    @Test
    fun queryAndSortUpdateHistoryState() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        uploadsFlow.value = listOf(
            task("1", "rep_a.pdf", 1000L, UploadStatus.COMPLETED, now - 2000),
            task("2", "rep_b.pdf", 9000L, UploadStatus.COMPLETED, now - 1000),
            task("3", "inv.pdf", 3000L, UploadStatus.COMPLETED, now - 500)
        )
        val viewModel = HistoryViewModel(fakeRepo)
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onQueryChanged("rep")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.totalMatches)

        viewModel.setSort(HistorySort.LARGEST)
        advanceUntilIdle()
        assertEquals("rep_b.pdf", viewModel.uiState.value.historyItems[0].fileName)
        job.cancel()
    }

    @Test
    fun repositoryActionsTriggerCorrectDelegations() = runTest(testDispatcher) {
        val viewModel = HistoryViewModel(fakeRepo)
        viewModel.deleteUpload("task-99")
        advanceUntilIdle()
        assertEquals(listOf("task-99"), deletedIds)

        viewModel.clearHistory()
        advanceUntilIdle()
        assertTrue(completedCleared)
    }
}
