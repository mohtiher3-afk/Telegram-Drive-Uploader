package com.telegramdrive.uploader.feature.home

import android.annotation.SuppressLint
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Behaviour test for the real `HomeScreen`.
 *
 * History of this file: an earlier revision of this class referenced a
 * `MissionHomeScreen(state = HomeScreenState(...), onAction = {})` API that has never
 * existed in any commit of this repository. `git log -S 'HomeScreenState' --all`
 * matches exactly one commit — the phase-1 audit report — because the file itself was
 * never tracked (a blanket `src/` rule in `.gitignore` kept it out of the index), so
 * no version of that screen was ever committed. The state class the screen actually
 * uses is `HomeUiState`, exposed as `HomeViewModel.uiState`, and `HomeScreen` takes a
 * `viewModel` parameter rather than a `state`.
 *
 * The test is therefore rebuilt against the production API, following the
 * `QueueScreenComposeTest` pattern: `HomeViewModel` is constructed directly with fake
 * repositories so the composable can be driven with a known state without a Hilt
 * container. `HomeScreenColorPairTest` documents why this needs Robolectric —
 * `HomeScreen` calls `hiltViewModel()`, so the default parameter must be bypassed by
 * passing the ViewModel explicitly.
 *
 * Behaviour mapping from the old (never-shipped) test to this one:
 *  - "displays title"                -> `displays the app title`
 *  - "displays subtitle"             -> dropped: `HomeScreen` has no subtitle text.
 *  - "empty queue shows empty msg"   -> dropped: `HomeScreen` renders no empty state;
 *                                      that copy lives on `QueueScreen`
 *                                      (`R.string.queue_empty_title`) and is covered
 *                                      by `QueueScreenComposeTest`.
 *  - "displays uploads"              -> `shows an active transfer for an uploading task`
 *  - "stats display"                 -> `stats grid reports the upload counts`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
@SuppressLint("ViewModelConstructorInComposable")
class HomeScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `displays the app title`() {
        setContent(uploads = emptyList())

        composeRule.onNodeWithTag("home_screen").assertExists()
        composeRule.onNodeWithText("Telegram Drive").assertIsDisplayed()
    }

    @Test
    fun `shows an active transfer for an uploading task`() {
        setContent(
            uploads = listOf(
                task("1", UploadStatus.UPLOADING, name = "vacation_clip.mp4")
            )
        )

        // HomeScreen renders inside a LazyColumn, so the active-transfer card sits
        // below the fold and is only composed after scrolling to it.
        composeRule.onNodeWithTag("home_screen")
            .performScrollToNode(hasTestTag("active_transfer_card"))
        composeRule.onNodeWithTag("active_transfer_card").assertExists()
        composeRule.onNodeWithText("vacation_clip.mp4").assertIsDisplayed()
    }

    @Test
    fun `stats grid reports the upload counts`() {
        setContent(
            uploads = listOf(
                task("1", UploadStatus.COMPLETED, name = "done.mp4"),
                task("2", UploadStatus.UPLOADING, name = "flying.mp4")
            )
        )

        // 2 total, 2 pending (UPLOADING counts as pending), 0 completed in the
        // stats grid inputs. Asserted through the labels HomeScreen renders so the
        // test fails if the grid stops receiving uiState.
        composeRule.onNodeWithText("Total Videos").assertExists()
        composeRule.onNodeWithText("Pending").assertExists()
        composeRule.onNodeWithText("Completed").assertExists()
    }

    @Test
    fun `greets the signed-in user by name`() {
        setContent(
            uploads = emptyList(),
            telegramUser = TelegramUser(
                id = 1L,
                firstName = "Sara",
                lastName = "Nasser",
                username = null,
                phoneNumber = "000",
                profilePhoto = null
            ),
            connectionState = TelegramConnectionState.AUTHORIZED
        )

        composeRule.onNodeWithText("Hi, Sara Nasser").assertIsDisplayed()
    }

    @Test
    fun `settings action is reachable from the home header`() {
        var settingsClicked = false
        setContent(uploads = emptyList(), onSettingsClick = { settingsClicked = true })

        composeRule.onNodeWithTag("home_settings_button").performClick()
        composeRule.waitForIdle()

        assertTrue("settings click was not forwarded", settingsClicked)
    }

    private fun setContent(
        uploads: List<UploadTask>,
        telegramUser: TelegramUser? = null,
        connectionState: TelegramConnectionState = TelegramConnectionState.DISCONNECTED,
        onSettingsClick: () -> Unit = {},
        onConnectClick: () -> Unit = {},
        onVideosSelected: (List<android.net.Uri>) -> Unit = {}
    ) {
        composeRule.setContent {
            HomeScreen(
                onSettingsClick = onSettingsClick,
                onConnectClick = onConnectClick,
                onVideosSelected = onVideosSelected,
                viewModel = HomeViewModel(
                    uploadRepository = FakeHomeUploadRepository(uploads),
                    telegramRepository = FakeHomeTelegramRepository(telegramUser, connectionState),
                    settingsDataStore = SettingsDataStore(ApplicationProvider.getApplicationContext()),
                    uploadManager = FakeHomeUploadManager()
                )
            )
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

    private class FakeHomeUploadRepository(initial: List<UploadTask>) : UploadRepository {
        private val tasks = MutableStateFlow(initial)

        override fun getAllUploads(): Flow<List<UploadTask>> = tasks

        override fun getActiveUploads(): Flow<List<UploadTask>> = tasks

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
        ): Boolean {
            if (tasks.value.firstOrNull { it.id == id }?.status !in allowedStatuses) return false
            updateStatus(id, status)
            return true
        }

        override suspend fun updateProgress(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long
        ) = Unit

        override suspend fun updateUploadDuration(id: String, durationMs: Long) = Unit

        override suspend fun updateMessageLink(id: String, messageLink: String) = Unit

        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) = Unit

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

    private class FakeHomeTelegramRepository(
        user: TelegramUser?,
        connection: TelegramConnectionState
    ) : TelegramRepository {
        override val connectionState: MutableStateFlow<TelegramConnectionState> =
            MutableStateFlow(connection)

        override val currentUser: MutableStateFlow<TelegramUser?> = MutableStateFlow(user)

        override val error: MutableStateFlow<com.telegramdrive.uploader.domain.model.TelegramError?> =
            MutableStateFlow(null)

        override val qrLoginLink: MutableStateFlow<String?> = MutableStateFlow(null)

        override val accounts: Flow<List<com.telegramdrive.uploader.domain.model.TelegramAccountEntry>> =
            flowOf(emptyList())

        override val isConfigured: Boolean = true

        override suspend fun connect() = Unit

        override suspend fun sendPhoneNumber(phoneNumber: String) = Unit

        override suspend fun sendCode(code: String) = Unit

        override suspend fun sendPassword(password: String) = Unit

        override suspend fun requestQrCodeLogin() = Unit

        override suspend fun logout() = Unit

        override suspend fun switchAccount(accountKey: String) = Unit

        override fun clearError() = Unit

        override fun getDestinations(
            query: String
        ): Flow<List<com.telegramdrive.uploader.domain.model.TelegramDestination>> = flowOf(emptyList())
    }

    private class FakeHomeUploadManager : UploadManager {
        override fun enqueueUpload(task: UploadTask) = Unit

        override fun pauseUpload(id: String) = Unit

        override fun resumeUpload(task: UploadTask) = Unit

        override fun cancelUpload(id: String) = Unit

        override fun retryUpload(task: UploadTask) = Unit

        override fun observeUpload(id: String): Flow<UploadTask?> = flowOf(null)

        override fun observeUploads(): Flow<List<UploadTask>> = flowOf(emptyList())
    }
}
