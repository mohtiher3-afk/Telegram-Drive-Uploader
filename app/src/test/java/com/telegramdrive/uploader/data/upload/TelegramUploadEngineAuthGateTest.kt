package com.telegramdrive.uploader.data.upload

import android.net.Uri
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.data.telegram.client.SendConfirmation
import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.data.telegram.client.TelegramUploadEvent
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import java.io.File
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TelegramUploadEngineAuthGateTest {

    private lateinit var sourceFile: File
    private lateinit var engine: TelegramUploadEngineImpl

    @Before
    fun setUp() {
        DiagnosticsManager.clearDiagnostics()
        sourceFile = File.createTempFile("upload-source-", ".mp4")
        sourceFile.writeBytes(ByteArray(1024))
        engine = TelegramUploadEngineImpl(
            streamingFileReader = FakeStreamingFileReader(),
            telegramClient = FakeTelegramClient(),
            uploadRepository = FakeUploadRepository()
        )
    }

    @Test
    fun `waits for authorized then uploads successfully`() = runTest {
        val client = FakeTelegramClient()
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository())
        client.uploadResult = flow { emit(TelegramUploadEvent.Completed(messageLink = "https://t.me/c/1/2")) }

        val deferred = async { engine.uploadFile(task()).toList() }

        // Auth is still pending; flip it to AUTHORIZED mid-collection. The engine must
        // wait instead of failing instantly, then proceed to deliver the upload.
        delay(100)
        client.state.value = TelegramConnectionState.AUTHORIZED

        val results = deferred.await()

        assertTrue(
            "Expected a Success, got only errors: ${results.filterIsInstance<UploadEngineResult.Error>()}",
            results.any { it is UploadEngineResult.Success }
        )
        assertTrue(results.none { it is UploadEngineResult.Error })
        assertEquals(
            "https://t.me/c/1/2",
            (results.first { it is UploadEngineResult.Success } as UploadEngineResult.Success).messageLink
        )
    }

    @Test
    fun `times out waiting for authorization and fails non-retryable`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.DISCONNECTED }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository())

        val error = engine.uploadFile(task()).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertTrue("Expected timeout message, got: ${error.message}", error.message.contains("Timed out"))
        assertEquals(false, error.isRetryable)
    }

    @Test
    fun `error state fails non-retryable immediately`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.ERROR }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository())

        val error = engine.uploadFile(task()).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertTrue("Expected authorization message, got: ${error.message}", error.message.contains("not authorized"))
        assertEquals(false, error.isRetryable)
    }

    @Test
    fun `staging failure logs the real exception so root cause is not swallowed`() = runTest {
        engine = TelegramUploadEngineImpl(
            FailingStreamingFileReader(),
            FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED },
            FakeUploadRepository()
        )

        // content:// source forces the staging path, exercising copyToFile.
        val contentTask = task().copy(sourceUri = "content://com.example/provider/upload-source.mp4")

        val error = engine.uploadFile(contentTask).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertEquals("Permission Denial: opening provider failed", error.message)
        assertTrue(
            "Staging IOException must be retryable so WorkManager retries, got: ${error.isRetryable}",
            error.isRetryable
        )

        val logged = DiagnosticsManager.events.value
        val failureEvent = logged.lastOrNull {
            it.category == DiagnosticCategory.UPLOAD_FAILED.name &&
                it.severity == DiagnosticSeverity.ERROR.name
        }
        assertTrue("Expected an UPLOAD_FAILED ERROR diagnostic event", failureEvent != null)
        assertTrue(
            "Expected the exception detail in the diagnostic message, got: ${failureEvent?.message}",
            failureEvent?.message?.contains("Permission Denial") == true
        )
        assertTrue(
            "Expected the exception class+message appended by DiagnosticsManager, got: ${failureEvent?.message}",
            failureEvent?.message?.contains("IOException") == true
        )
    }

    @Test
    fun `source that cannot be opened fails fast and non-retryable`() = runTest {
        engine = TelegramUploadEngineImpl(
            UnopenableStreamingFileReader(),
            FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED },
            FakeUploadRepository()
        )

        // content:// source forces the staging path; a dead grant surfaces as
        // FileNotFoundException from copyToFile and must NOT burn retry attempts.
        val contentTask = task().copy(sourceUri = "content://com.example/provider/gone.mp4")

        val error = engine.uploadFile(contentTask).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertTrue(
            "Expected a clear re-select message, got: ${error.message}",
            error.message.contains("no longer readable")
        )
        assertEquals(false, error.isRetryable)
    }

    @Test
    fun `persists provisional id on send then completes without resending`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        val repository = FakeUploadRepository()
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, repository)
        client.uploadResult = flow {
            emit(TelegramUploadEvent.Progress(512L, 1024L))
            emit(TelegramUploadEvent.MessageSent(provisionalMessageId = 777L))
            emit(TelegramUploadEvent.Completed(messageLink = "https://t.me/c/1/2"))
        }

        val results = engine.uploadFile(task()).toList()

        assertTrue(results.any { it is UploadEngineResult.Success })
        assertEquals("test-upload" to 777L, repository.lastProvisional)
    }

    @Test
    fun `retry with provisional id awaits confirmation instead of resending`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        client.bufferedConfirmation = SendConfirmation(chatId = 123L, messageId = 999L, messageLink = "https://t.me/c/1/999")
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository())

        val results = engine.uploadFile(task().copy(provisionalMessageId = 777L)).toList()

        assertEquals(0, client.uploadCalls)
        val success = results.filterIsInstance<UploadEngineResult.Success>().firstOrNull()
        assertTrue("Expected Success from buffered confirmation, got: $results", success != null)
        assertEquals("https://t.me/c/1/999", success?.messageLink)
        assertTrue(results.none { it is UploadEngineResult.Error })
    }

    @Test
    fun `provisional retry without confirmation fails non-retryable, never resends`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        // bufferedConfirmation and awaitedConfirmation stay null: the send may have
        // happened, but nothing confirms it — resending would duplicate the message.
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository())

        val results = engine.uploadFile(task().copy(provisionalMessageId = 777L)).toList()
        val error = results.filterIsInstance<UploadEngineResult.Error>().firstOrNull()

        assertEquals(0, client.uploadCalls)
        assertTrue("Expected ambiguous-failure error, got: $results", error != null)
        assertEquals(false, error?.isRetryable)
    }

    private fun task(): UploadTask = UploadTask(
        id = "test-upload",
        sourceUri = Uri.fromFile(sourceFile).toString(),
        fileName = "test.mp4",
        fileSize = 1024L,
        destinationId = 123L,
        status = UploadStatus.QUEUED
    )

    private class FakeTelegramClient : TelegramClient {
        val state = MutableStateFlow(TelegramConnectionState.CONNECTING)
        var uploadResult: Flow<TelegramUploadEvent> = flow { emit(TelegramUploadEvent.Completed("https://t.me/c/1/2")) }
        var uploadCalls = 0
        var bufferedConfirmation: SendConfirmation? = null
        var awaitedConfirmation: SendConfirmation? = null

        override val connectionState: StateFlow<TelegramConnectionState> get() = state
        override val currentUser: StateFlow<TelegramUser?> = MutableStateFlow(null)
        override val error: StateFlow<TelegramError?> = MutableStateFlow(null)
        override val qrLoginLink: StateFlow<String?> = MutableStateFlow(null)
        override val accounts: Flow<List<TelegramAccountEntry>> = flow { emit(emptyList()) }
        override val isConfigured: Boolean = true

        override suspend fun connect() {}
        override suspend fun sendPhoneNumber(phoneNumber: String) {}
        override suspend fun sendCode(code: String) {}
        override suspend fun sendPassword(password: String) {}
        override suspend fun requestQrCodeLogin() {}
        override suspend fun logout() {}
        override suspend fun switchAccount(accountKey: String) {}
        override fun clearError() {}

        override fun getDestinations(query: String): Flow<List<TelegramDestination>> = flow { emit(emptyList()) }
        override fun uploadLocalDocument(task: UploadTask, localPath: String): Flow<TelegramUploadEvent> {
            uploadCalls++
            return uploadResult
        }
        override fun cancelActiveUploads() {}
        override fun takeBufferedSendSuccess(oldMessageId: Long): SendConfirmation? = bufferedConfirmation
        override suspend fun awaitSendConfirmation(chatId: Long, oldMessageId: Long, timeoutMs: Long): SendConfirmation? =
            awaitedConfirmation
    }

    private class FakeUploadRepository : UploadRepository {
        val tasks = mutableMapOf<String, UploadTask>()
        var lastProvisional: Pair<String, Long>? = null

        override fun getAllUploads(): Flow<List<UploadTask>> = flow { emit(tasks.values.toList()) }
        override fun getActiveUploads(): Flow<List<UploadTask>> = flow { emit(tasks.values.toList()) }
        override suspend fun getUploadById(id: String): UploadTask? = tasks[id]
        override fun observeUploadById(id: String): Flow<UploadTask?> = flow { emit(tasks[id]) }
        override suspend fun insertUpload(upload: UploadTask) { tasks[upload.id] = upload }
        override suspend fun updateStatus(id: String, status: UploadStatus) {
            tasks[id]?.let { tasks[id] = it.copy(status = status) }
        }
        override suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>) {
            tasks[id]?.let { if (it.status in allowedStatuses) tasks[id] = it.copy(status = status) }
        }
        override suspend fun updateProgress(
            id: String, uploadedBytes: Long, totalBytes: Long, progress: Float,
            speed: Long, averageSpeed: Long, eta: Long
        ) {
        }
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {}
        override suspend fun updateMessageLink(id: String, messageLink: String) {
            tasks[id]?.let { tasks[id] = it.copy(messageLink = messageLink) }
        }
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {
            lastProvisional = id to messageId
            tasks[id]?.let { tasks[id] = it.copy(provisionalMessageId = messageId) }
        }
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) { tasks.remove(id) }
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() { tasks.clear() }
    }

    private class FakeStreamingFileReader : StreamingFileReader {
        override fun getFileSize(uri: Uri): Long = 1024L
        override fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray = ByteArray(0)
        override fun copyToFile(uri: Uri, destination: File): Long {
            destination.parentFile?.mkdirs()
            destination.writeBytes(ByteArray(1024))
            return 1024L
        }
    }

    private class FailingStreamingFileReader : StreamingFileReader {
        override fun getFileSize(uri: Uri): Long = 1024L
        override fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray = ByteArray(0)
        override fun copyToFile(uri: Uri, destination: File): Long {
            throw IOException("Permission Denial: opening provider failed")
        }
    }

    private class UnopenableStreamingFileReader : StreamingFileReader {
        override fun getFileSize(uri: Uri): Long = 1024L
        override fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray = ByteArray(0)
        override fun copyToFile(uri: Uri, destination: File): Long {
            throw java.io.FileNotFoundException("Source is no longer readable: $uri. Re-select the file.")
        }
    }
}