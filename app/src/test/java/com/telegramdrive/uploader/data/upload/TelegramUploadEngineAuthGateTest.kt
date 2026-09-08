package com.telegramdrive.uploader.data.upload

import android.net.Uri
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.data.telegram.client.TelegramUploadEvent
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
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
            telegramClient = FakeTelegramClient()
        )
    }

    @Test
    fun `waits for authorized then uploads successfully`() = runTest {
        val client = FakeTelegramClient()
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client)
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
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client)

        val error = engine.uploadFile(task()).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertTrue("Expected timeout message, got: ${error.message}", error.message.contains("Timed out"))
        assertEquals(false, error.isRetryable)
    }

    @Test
    fun `error state fails non-retryable immediately`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.ERROR }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client)

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
            FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
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
            FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
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
        override fun uploadLocalDocument(task: UploadTask, localPath: String): Flow<TelegramUploadEvent> = uploadResult
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