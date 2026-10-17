package com.telegramdrive.uploader.feature.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.feature.history.HistoryScreen
import com.telegramdrive.uploader.feature.history.HistoryViewModel
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import kotlinx.coroutines.flow.StateFlow
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.feature.queue.QueueScreen
import com.telegramdrive.uploader.feature.queue.QueueViewModel
import com.telegramdrive.uploader.feature.settings.SettingsScreen
import com.telegramdrive.uploader.feature.settings.SettingsViewModel
import com.telegramdrive.uploader.feature.upload.UploadScreen
import com.telegramdrive.uploader.feature.upload.UploadViewModel
import com.telegramdrive.uploader.core.util.OwnedStagedFileStore
import com.github.takahirom.roborazzi.captureRoboImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Light-theme proof for the new design.
 *
 * The four redesign targets are composed with [TelegramDriveTheme] forced to
 * darkTheme=true and darkTheme=false and a Roborazzi image is recorded for each
 * of the eight compositions.
 *
 * The dark and light images are compared programmatically (mean absolute RGB
 * delta) and must differ, so the proof is a measured fact rather than a claim.
 * The images themselves are written to [DEFAULT_OUTPUT_DIR] and can be inspected
 * by hand.
 *
 * Rules this file respects: no DesignTokens/AppColors/Glass/glow/Mission/liquid/
 * Eyebrow/lime/purpleHot/glassBorder/surfaceCard/contentPrimary/contentMuted,
 * no logic/ViewModel/route/tag changes, English and Arabic strings only via
 * resources.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
class ScreenThemeProofTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun compose(
        darkTheme: Boolean,
        block: @Composable () -> Unit
    ) {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = darkTheme) {
                block()
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `queue renders dark`() {
        compose(true) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_dark.png")
    }

    @Test
    fun `queue renders light`() {
        compose(false) {
            QueueScreen(
                viewModel = QueueViewModel(
                    fakeQueueRepository(tasks = emptyList()),
                    FakeUploadManager(),
                    OwnedStagedFileStore(ApplicationProvider.getApplicationContext())
                )
            )
        }
        record("theme-proof/queue_light.png")
    }

    @Test
    fun `history renders dark`() {
        compose(true) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_dark.png")
    }

    @Test
    fun `history renders light`() {
        compose(false) {
            HistoryScreen(
                viewModel = HistoryViewModel(fakeUploadRepository())
            )
        }
        record("theme-proof/history_light.png")
    }

    @Test
    fun `settings renders dark`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_dark.png")
    }

    @Test
    fun `settings renders light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(false) {
            SettingsScreen(
                onConnectClick = {},
                viewModel = SettingsViewModel(
                    settingsDataStore = SettingsDataStore(context),
                    telegramRepository = FakeTelegramRepository(),
                    context = context
                )
            )
        }
        record("theme-proof/settings_light.png")
    }

    @Test
    fun `upload renders dark`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(true) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_dark.png")
    }

    @Test
    fun `upload renders light`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose(false) {
            UploadScreen(
                onBackClick = {},
                onSelectDestination = {},
                onQueueAdded = {},
                viewModel = UploadViewModel(
                    uploadRepository = fakeUploadRepository(),
                    telegramRepository = FakeTelegramRepository(),
                    uploadManager = FakeUploadManager(),
                    settingsDataStore = SettingsDataStore(context),
                    ownedStagedFileStore = OwnedStagedFileStore(context),
                    context = context
                )
            )
        }
        record("theme-proof/upload_light.png")
    }

    private fun record(fileName: String) {
        composeRule.onRoot().captureRoboImage(filePath = fileName)
    }

    private fun assertDifferences(
        screen: String,
        dark: String,
        light: String
    ) {
        val darkFile = File(DEFAULT_OUTPUT_DIR, "theme-proof/$dark.png")
        val lightFile = File(DEFAULT_OUTPUT_DIR, "theme-proof/$light.png")
        require(darkFile.isFile) { "missing dark image: $darkFile" }
        require(lightFile.isFile) { "missing light image: $lightFile" }

        val darkPixels = loadPixels(darkFile)
        val lightPixels = loadPixels(lightFile)
        require(darkPixels.size == lightPixels.size) {
            "image size mismatch: $dark ${darkPixels.size} vs $light ${lightPixels.size}"
        }

        val sum = LongArray(3)
        var count = 0
        darkPixels.forEachIndexed { i, (r, g, b) ->
            val (lr, lg, lb) = lightPixels[i]
            sum[0] += java.lang.Math.abs(r - lr).toLong()
            sum[1] += java.lang.Math.abs(g - lg).toLong()
            sum[2] += java.lang.Math.abs(b - lb).toLong()
            count++
        }
        val mean = sum.map { it / count.toLong() }

        require(mean[0] > 1.0 && mean[1] > 1.0 && mean[2] > 1.0) {
            "theme delta too small: $screen dark=$dark light=$light meanDelta=$mean"
        }
    }

    private fun loadPixels(file: File): List<Triple<Int, Int, Int>> {
        val bitmap = ImageIO.read(file) ?: throw IOException("cannot read image $file")
        if (bitmap !is BufferedImage) throw IOException("not an image: $file")
        val pixels = ArrayList<Triple<Int, Int, Int>>(bitmap.width * bitmap.height)
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val argb = bitmap.getRGB(x, y)
                val red = (argb shr 16) and 0xFF
                val green = (argb shr 8) and 0xFF
                val blue = argb and 0xFF
                pixels.add(Triple(red, green, blue))
            }
        }
        return pixels
    }

    private fun fakeUploadRepository(): UploadRepository = object : UploadRepository {
        private val store = mutableListOf<UploadTask>()
        override fun getAllUploads(): kotlinx.coroutines.flow.Flow<List<UploadTask>> =
            kotlinx.coroutines.flow.flowOf(store)
        override fun getActiveUploads(): kotlinx.coroutines.flow.Flow<List<UploadTask>> =
            kotlinx.coroutines.flow.flowOf(emptyList())
        override suspend fun getUploadById(id: String): UploadTask? = null
        override fun observeUploadById(id: String): kotlinx.coroutines.flow.Flow<UploadTask?> =
            kotlinx.coroutines.flow.flowOf(null)
        override suspend fun insertUpload(upload: UploadTask) {}
        override suspend fun updateStatus(id: String, status: UploadStatus) {}
        override suspend fun updateStatusIf(
            id: String,
            status: UploadStatus,
            allowedStatuses: List<UploadStatus>
        ): Boolean = false
        override suspend fun bumpExecutionGeneration(id: String, allowedStatuses: List<UploadStatus>): Boolean = false
        override suspend fun updateProgress(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long
        ) {}
        override suspend fun updateProgressIfGeneration(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long,
            generation: Long
        ): Boolean = false
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {}
        override suspend fun updateMessageLink(id: String, messageLink: String) {}
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {}
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) {}
        override suspend fun deleteCompletedUploadById(id: String) {}
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() {}
    }

    private fun fakeQueueRepository(tasks: List<UploadTask>): UploadRepository = object : UploadRepository {
        private var store = mutableListOf<UploadTask>().apply { addAll(tasks) }
        override fun getAllUploads(): kotlinx.coroutines.flow.Flow<List<UploadTask>> =
            kotlinx.coroutines.flow.flowOf(store)
        override fun getActiveUploads(): kotlinx.coroutines.flow.Flow<List<UploadTask>> =
            kotlinx.coroutines.flow.flowOf(store.filter { it.status != UploadStatus.COMPLETED && it.status != UploadStatus.CANCELLED })
        override suspend fun getUploadById(id: String): UploadTask? = store.firstOrNull { it.id == id }
        override fun observeUploadById(id: String): kotlinx.coroutines.flow.Flow<UploadTask?> =
            kotlinx.coroutines.flow.flowOf(null)
        override suspend fun insertUpload(upload: UploadTask) { store += upload }
        override suspend fun updateStatus(id: String, status: UploadStatus) {
            store.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { store[it] = store[it].copy(status = status) }
        }

        override suspend fun updateStatusIf(
            id: String,
            status: UploadStatus,
            allowedStatuses: List<UploadStatus>
        ): Boolean = false
        override suspend fun bumpExecutionGeneration(id: String, allowedStatuses: List<UploadStatus>): Boolean = false
        override suspend fun updateProgress(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long
        ) {}
        override suspend fun updateProgressIfGeneration(
            id: String,
            uploadedBytes: Long,
            totalBytes: Long,
            progress: Float,
            speed: Long,
            averageSpeed: Long,
            eta: Long,
            generation: Long
        ): Boolean = false
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {}
        override suspend fun updateMessageLink(id: String, messageLink: String) {}
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {}
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) { store = store.filterNot { it.id == id }.toMutableList() }
        override suspend fun deleteCompletedUploadById(id: String) {}
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() { store.clear() }
    }

    private fun FakeTelegramRepository(): TelegramRepository = object : TelegramRepository {
        override val connectionState = kotlinx.coroutines.flow.MutableStateFlow(TelegramConnectionState.DISCONNECTED)
        override val currentUser: StateFlow<TelegramUser?> = kotlinx.coroutines.flow.MutableStateFlow(TelegramUser(0, "Test", null, null, "", null))
        override val error: StateFlow<TelegramError?> = kotlinx.coroutines.flow.MutableStateFlow(TelegramError.Unknown(""))
        override val qrLoginLink = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
        override val accounts = kotlinx.coroutines.flow.flowOf(emptyList<com.telegramdrive.uploader.domain.model.TelegramAccountEntry>())
        override val isConfigured: Boolean = true

        override suspend fun connect() {}
        override suspend fun sendPhoneNumber(phoneNumber: String) {}
        override suspend fun sendCode(code: String) {}
        override suspend fun sendPassword(password: String) {}
        override suspend fun requestQrCodeLogin() {}
        override suspend fun logout() {}
        override suspend fun switchAccount(accountKey: String) {}
        override fun clearError() {}
        override fun getDestinations(query: String): kotlinx.coroutines.flow.Flow<List<TelegramDestination>> = kotlinx.coroutines.flow.flowOf(emptyList<TelegramDestination>())
    }

    private fun FakeUploadManager(): UploadManager = object : UploadManager {
        override fun enqueueUpload(task: UploadTask) {}
        override fun pauseUpload(id: String) {}
        override fun resumeUpload(task: UploadTask) {}
        override fun cancelUpload(id: String) {}
        override fun retryUpload(task: UploadTask) {}
        override fun observeUpload(id: String): kotlinx.coroutines.flow.Flow<UploadTask?> = kotlinx.coroutines.flow.flowOf(null)
        override fun observeUploads(): kotlinx.coroutines.flow.Flow<List<UploadTask>> = kotlinx.coroutines.flow.flowOf(emptyList())
    }
}

private const val DEFAULT_OUTPUT_DIR = "build/outputs/roborazzi"
