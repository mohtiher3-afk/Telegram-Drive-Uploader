package com.telegramdrive.uploader.feature.home

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.domain.model.TelegramAccountEntry
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeViewModelUiStateTest {

    private val testDispatcher = StandardTestDispatcher()

    private val uploadsFlow = MutableStateFlow<List<UploadTask>>(emptyList())
    private val connectionFlow = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
    private val currentUserFlow = MutableStateFlow<TelegramUser?>(null)

    private val fakeUploadRepository = object : UploadRepository {
        override fun getAllUploads(): Flow<List<UploadTask>> = uploadsFlow
        override fun getActiveUploads(): Flow<List<UploadTask>> = emptyFlow()
        override suspend fun getUploadById(id: String): UploadTask? = null
        override fun observeUploadById(id: String): Flow<UploadTask?> = emptyFlow()
        override suspend fun insertUpload(upload: UploadTask) {}
        override suspend fun updateStatus(id: String, status: UploadStatus) {}
        override suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>): Boolean = true
        override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long) {}
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {}
        override suspend fun updateMessageLink(id: String, messageLink: String) {}
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {}
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) {}
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() {}
    }

    private val fakeTelegramRepository = object : TelegramRepository {
        override val connectionState: StateFlow<TelegramConnectionState> = connectionFlow
        override val currentUser: StateFlow<TelegramUser?> = currentUserFlow
        override val error: StateFlow<TelegramError?> = MutableStateFlow(null)
        override val qrLoginLink: StateFlow<String?> = MutableStateFlow(null)
        override val accounts: Flow<List<TelegramAccountEntry>> = emptyFlow()
        override val isConfigured: Boolean = true

        override suspend fun connect() {}
        override suspend fun sendPhoneNumber(phoneNumber: String) {}
        override suspend fun sendCode(code: String) {}
        override suspend fun sendPassword(password: String) {}
        override suspend fun requestQrCodeLogin() {}
        override suspend fun logout() {}
        override suspend fun switchAccount(accountKey: String) {}
        override fun clearError() {}
        override fun getDestinations(query: String): Flow<List<TelegramDestination>> = emptyFlow()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testTask(id: String, status: UploadStatus, size: Long = 1000L): UploadTask = UploadTask(
        id = id,
        sourceUri = "content://media/$id",
        fileName = "file_$id.mp4",
        fileSize = size,
        totalBytes = size,
        status = status
    )

    @Test
    fun uiStateReflectsUploadAggregationsAndConnection() = runTest(testDispatcher) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsDataStore = SettingsDataStore(context)
        val viewModel = HomeViewModel(fakeUploadRepository, fakeTelegramRepository, settingsDataStore)

        val collectJob = launch { viewModel.uiState.collect {} }

        val tasks = listOf(
            testTask("t1", UploadStatus.QUEUED, 100L),
            testTask("t2", UploadStatus.UPLOADING, 200L),
            testTask("t3", UploadStatus.COMPLETED, 300L),
            testTask("t4", UploadStatus.FAILED, 400L),
            testTask("t5", UploadStatus.CANCELLED, 500L)
        )
        val user = TelegramUser(
            id = 999L,
            firstName = "Test",
            lastName = "User",
            username = "tester",
            phoneNumber = "+123456789",
            profilePhoto = null
        )

        uploadsFlow.value = tasks
        connectionFlow.value = TelegramConnectionState.AUTHORIZED
        currentUserFlow.value = user

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.totalVideosCount)
        assertEquals(1500L, state.totalSize)
        assertEquals(1, state.completedCount)
        assertEquals(3, state.pendingCount)
        assertEquals(2, state.activeUploads.size)
        assertEquals(3, state.recentActivity.size)
        assertEquals(TelegramConnectionState.AUTHORIZED, state.telegramConnectionState)
        assertEquals(user, state.telegramUser)

        collectJob.cancel()
    }
}
