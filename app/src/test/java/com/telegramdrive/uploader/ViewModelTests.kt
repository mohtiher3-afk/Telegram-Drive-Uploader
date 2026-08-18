package com.telegramdrive.uploader

import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.feature.home.HomeViewModel
import com.telegramdrive.uploader.feature.queue.QueueViewModel
import com.telegramdrive.uploader.feature.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTests {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeUploadRepository
    private lateinit var fakeTelegramRepository: FakeTelegramRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeUploadRepository()
        fakeTelegramRepository = FakeTelegramRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testHomeViewModelStatsCalculation() = runTest {
        val homeViewModel = HomeViewModel(fakeRepository, fakeTelegramRepository)

        // Prepopulate repository with different upload statuses
        val uploads = listOf(
            UploadTask(
                id = "1", sourceUri = "uri1", fileName = "v1.mp4", fileSize = 1000L, mimeType = "video/mp4",
                destinationId = 123L, status = UploadStatus.COMPLETED, progress = 1f, uploadedBytes = 1000L, totalBytes = 1000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 123L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            ),
            UploadTask(
                id = "2", sourceUri = "uri2", fileName = "v2.mp4", fileSize = 2000L, mimeType = "video/mp4",
                destinationId = 124L, status = UploadStatus.QUEUED, progress = 0f, uploadedBytes = 0L, totalBytes = 2000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 124L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            ),
            UploadTask(
                id = "3", sourceUri = "uri3", fileName = "v3.mp4", fileSize = 3000L, mimeType = "video/mp4",
                destinationId = 125L, status = UploadStatus.FAILED, progress = 0.5f, uploadedBytes = 1500L, totalBytes = 3000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 125L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            )
        )
        fakeRepository.insertUploads(uploads)

        // Advance dispatcher to collect flows
        testScheduler.advanceUntilIdle()

        val uiState = homeViewModel.uiState.first()
        assertEquals(3, uiState.totalVideosCount)
        assertEquals(6000L, uiState.totalSize)
        assertEquals(1, uiState.completedCount)
        assertEquals(2, uiState.pendingCount) // QUEUED + FAILED are pending
    }

    @Test
    fun testQueueViewModelFiltering() = runTest {
        val fakeUploadManager = FakeUploadManager()
        val queueViewModel = QueueViewModel(fakeRepository, fakeUploadManager)

        val uploads = listOf(
            UploadTask(
                id = "1", sourceUri = "uri1", fileName = "v1.mp4", fileSize = 1000L, mimeType = "video/mp4",
                destinationId = 100L, status = UploadStatus.COMPLETED, progress = 1f, uploadedBytes = 1000L, totalBytes = 1000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 123L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            ),
            UploadTask(
                id = "2", sourceUri = "uri2", fileName = "v2.mp4", fileSize = 2000L, mimeType = "video/mp4",
                destinationId = 200L, status = UploadStatus.QUEUED, progress = 0f, uploadedBytes = 0L, totalBytes = 2000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 124L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            )
        )
        fakeRepository.insertUploads(uploads)

        testScheduler.advanceUntilIdle()

        val uiState = queueViewModel.uiState.first()
        assertEquals(1, uiState.queueItems.size)
        assertEquals("2", uiState.queueItems[0].id)
    }

    @Test
    fun testHistoryViewModelFilteringAndClear() = runTest {
        val historyViewModel = HistoryViewModel(fakeRepository)

        val uploads = listOf(
            UploadTask(
                id = "1", sourceUri = "uri1", fileName = "v1.mp4", fileSize = 1000L, mimeType = "video/mp4",
                destinationId = 100L, status = UploadStatus.COMPLETED, progress = 1f, uploadedBytes = 1000L, totalBytes = 1000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 123L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            ),
            UploadTask(
                id = "2", sourceUri = "uri2", fileName = "v2.mp4", fileSize = 2000L, mimeType = "video/mp4",
                destinationId = 200L, status = UploadStatus.QUEUED, progress = 0f, uploadedBytes = 0L, totalBytes = 2000L,
                speed = 0L, averageSpeed = 0L, eta = 0L, createdAt = 124L, startedAt = null, completedAt = null,
                lastError = null, retryCount = 0, thumbnailPath = null, duration = 0L, width = 1920, height = 1080
            )
        )
        fakeRepository.insertUploads(uploads)

        testScheduler.advanceUntilIdle()

        var uiState = historyViewModel.uiState.first()
        assertEquals(1, uiState.historyItems.size)
        assertEquals("1", uiState.historyItems[0].id)

        // Clear history
        historyViewModel.clearHistory()
        testScheduler.advanceUntilIdle()

        uiState = historyViewModel.uiState.first()
        assertEquals(0, uiState.historyItems.size)
    }

    // A robust, self-contained mock repository for VM testing
    class FakeUploadRepository : UploadRepository {
        private val uploads = MutableStateFlow<List<UploadTask>>(emptyList())

        override fun getAllUploads(): Flow<List<UploadTask>> = uploads
        override fun getActiveUploads(): Flow<List<UploadTask>> = uploads // Simplified for testing

        override suspend fun getUploadById(id: String): UploadTask? = uploads.value.find { it.id == id }
        override fun observeUploadById(id: String): Flow<UploadTask?> = flow { emit(getUploadById(id)) }

        override suspend fun insertUpload(upload: UploadTask) {
            uploads.value = uploads.value + upload
        }
        
        suspend fun insertUploads(uploadsList: List<UploadTask>) {
            uploads.value = uploads.value + uploadsList
        }

        override suspend fun updateStatus(id: String, status: UploadStatus) {
            uploads.value = uploads.value.map { if (it.id == id) it.copy(status = status) else it }
        }
        
        override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long) {
            // Simplified for testing
        }

        override suspend fun deleteUploadById(id: String) {
            uploads.value = uploads.value.filter { it.id != id }
        }

        override suspend fun deleteCompletedUploads() {
             uploads.value = uploads.value.filter { it.status != UploadStatus.COMPLETED }
        }

        override suspend fun clearAllUploads() {
            uploads.value = emptyList()
        }
    }

    class FakeUploadManager : UploadManager {
        override fun enqueueUpload(task: UploadTask) {}
        override fun pauseUpload(id: String) {}
        override fun resumeUpload(task: UploadTask) {}
        override fun cancelUpload(id: String) {}
        override fun retryUpload(task: UploadTask) {}
        override fun observeUpload(id: String): Flow<UploadTask?> = flowOf(null)
        override fun observeUploads(): Flow<List<UploadTask>> = flowOf(emptyList())
    }

    class FakeTelegramRepository : TelegramRepository {
        override val connectionState = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
        override val currentUser = MutableStateFlow<TelegramUser?>(null)
        override val error = MutableStateFlow<TelegramError?>(null)
        override val isConfigured: Boolean = true

        override suspend fun connect() {}
        override suspend fun sendPhoneNumber(phoneNumber: String) {}
        override suspend fun sendCode(code: String) {}
        override suspend fun sendPassword(password: String) {}
        override suspend fun logout() {}
        override fun clearError() {}
        override fun getDestinations(query: String) = flowOf(emptyList<TelegramDestination>())
    }
}
