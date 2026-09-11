package com.telegramdrive.uploader.data.upload

import android.net.Uri
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.data.local.database.UploadDao
import com.telegramdrive.uploader.data.local.database.UploadEntity
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
import org.junit.Assert.assertNull
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
            uploadRepository = FakeUploadRepository(),
            uploadDao = FakeUploadDao()
        )
    }

    @Test
    fun `waits for authorized then uploads successfully`() = runTest {
        val client = FakeTelegramClient()
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), FakeUploadDao())
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
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), FakeUploadDao())

        val error = engine.uploadFile(task()).toList()
            .filterIsInstance<UploadEngineResult.Error>()
            .first()

        assertTrue("Expected timeout message, got: ${error.message}", error.message.contains("Timed out"))
        assertEquals(false, error.isRetryable)
    }

    @Test
    fun `error state fails non-retryable immediately`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.ERROR }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), FakeUploadDao())

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
            FakeUploadRepository(),
            FakeUploadDao()
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
            FakeUploadRepository(),
            FakeUploadDao()
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
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, repository, FakeUploadDao())
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
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), FakeUploadDao())

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
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), FakeUploadDao())

        val results = engine.uploadFile(task().copy(provisionalMessageId = 777L)).toList()
        val error = results.filterIsInstance<UploadEngineResult.Error>().firstOrNull()

        assertEquals(0, client.uploadCalls)
        assertTrue("Expected ambiguous-failure error, got: $results", error != null)
        assertEquals(false, error?.isRetryable)
    }

    @Test
    fun `already-confirmed send short-circuits to success without touching TDLib`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.DISCONNECTED }
        val dao = FakeUploadDao().also {
            it.rows["test-upload"] = entity(finalId = 999L, link = "https://t.me/c/1/999")
        }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), dao)

        val results = engine.uploadFile(task()).toList()

        assertEquals(0, client.uploadCalls)
        val success = results.filterIsInstance<UploadEngineResult.Success>().firstOrNull()
        assertTrue("Expected fast-path Success, got: $results", success != null)
        assertEquals("https://t.me/c/1/999", success?.messageLink)
        assertTrue(results.none { it is UploadEngineResult.Error })
    }

    @Test
    fun `retry confirmation persists final state and clears send bookkeeping`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        client.bufferedConfirmation = SendConfirmation(chatId = 123L, messageId = 999L, messageLink = "https://t.me/c/1/999")
        val dao = FakeUploadDao().also {
            it.rows["test-upload"] = entity(provisional = 777L, dispatched = true)
        }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), dao)

        val results = engine.uploadFile(task()).toList()

        assertTrue(results.any { it is UploadEngineResult.Success })
        val row = dao.rows["test-upload"]!!
        assertEquals(999L, row.finalMessageId)
        assertEquals("https://t.me/c/1/999", row.messageLink)
        assertNull("Provisional id must be cleared once resolved", row.provisionalMessageId)
        assertTrue("Dispatch flag must be cleared once resolved", !row.sendDispatched)
    }

    @Test
    fun `awaited confirmation also persists final state`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        client.awaitedConfirmation = SendConfirmation(chatId = 123L, messageId = 999L, messageLink = "https://t.me/c/1/999")
        val dao = FakeUploadDao().also {
            it.rows["test-upload"] = entity(provisional = 777L, dispatched = true)
        }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), dao)

        val results = engine.uploadFile(task()).toList()

        assertTrue(results.any { it is UploadEngineResult.Success })
        assertEquals(999L, dao.rows["test-upload"]?.finalMessageId)
    }

    @Test
    fun `provisional persist failure fails the attempt non-retryably`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        client.uploadResult = flow {
            emit(TelegramUploadEvent.MessageSent(provisionalMessageId = 777L))
            emit(TelegramUploadEvent.Completed(messageLink = "https://t.me/c/1/2"))
        }
        val repository = FakeUploadRepository().also { it.failProvisionalPersist = true }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, repository, FakeUploadDao())

        val results = engine.uploadFile(task()).toList()
        val error = results.filterIsInstance<UploadEngineResult.Error>().firstOrNull()

        assertTrue("Expected an error, got: $results", error != null)
        assertEquals("A bookkeeping write failure must never be retried blindly", false, error?.isRetryable)
        assertTrue(results.none { it is UploadEngineResult.Success })
    }

    @Test
    fun `dispatch flag without provisional id delegates resolution to the client`() = runTest {
        val client = FakeTelegramClient().also { it.state.value = TelegramConnectionState.AUTHORIZED }
        val dao = FakeUploadDao().also {
            it.rows["test-upload"] = entity(dispatched = true)
        }
        engine = TelegramUploadEngineImpl(FakeStreamingFileReader(), client, FakeUploadRepository(), dao)

        val results = engine.uploadFile(task()).toList()

        assertEquals("Client must be asked to resolve/re-send, not the engine", 1, client.uploadCalls)
        assertTrue(results.any { it is UploadEngineResult.Success })
    }

    private fun task(): UploadTask = UploadTask(
        id = "test-upload",
        sourceUri = Uri.fromFile(sourceFile).toString(),
        fileName = "test.mp4",
        fileSize = 1024L,
        destinationId = 123L,
        status = UploadStatus.QUEUED
    )

    private fun entity(
        provisional: Long? = null,
        dispatched: Boolean = false,
        finalId: Long? = null,
        link: String? = null
    ): UploadEntity = UploadEntity(
        id = "test-upload",
        sourceUri = "file:///tmp/test.mp4",
        fileName = "test.mp4",
        fileSize = 1024L,
        mimeType = "video/mp4",
        destinationId = 123L,
        status = "RETRYING",
        progress = 0f,
        uploadedBytes = 0L,
        totalBytes = 1024L,
        speed = 0L,
        averageSpeed = 0L,
        eta = 0L,
        createdAt = 0L,
        startedAt = null,
        completedAt = null,
        lastError = null,
        retryCount = 0,
        thumbnailPath = null,
        duration = 0L,
        width = 0,
        height = 0,
        scheduledAt = null,
        uploadDurationMs = 0L,
        messageLink = link,
        provisionalMessageId = provisional,
        sendDispatched = dispatched,
        finalMessageId = finalId
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
        var failProvisionalPersist = false

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
            if (failProvisionalPersist) {
                throw java.sql.SQLException("Simulated Room write failure")
            }
            lastProvisional = id to messageId
            tasks[id]?.let { tasks[id] = it.copy(provisionalMessageId = messageId) }
        }
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) { tasks.remove(id) }
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() { tasks.clear() }
    }

    private class FakeUploadDao : UploadDao {
        val rows = mutableMapOf<String, UploadEntity>()

        private fun update(id: String, transform: (UploadEntity) -> UploadEntity) {
            rows[id]?.let { rows[id] = transform(it) }
        }

        override fun getAllUploads(): Flow<List<UploadEntity>> = flow { emit(rows.values.toList()) }
        override suspend fun getInterruptedUploads(): List<UploadEntity> =
            rows.values.filter { it.status == "PREPARING" || it.status == "UPLOADING" }
        override fun getActiveUploads(): Flow<List<UploadEntity>> = flow {
            emit(rows.values.filter { it.status in listOf("QUEUED", "PREPARING", "UPLOADING", "RETRYING") })
        }
        override suspend fun getUploadById(id: String): UploadEntity? = rows[id]
        override fun observeUploadById(id: String): Flow<UploadEntity?> = flow { emit(rows[id]) }
        override suspend fun insertUpload(upload: UploadEntity) { rows[upload.id] = upload }
        override suspend fun insertUploads(uploads: List<UploadEntity>) { uploads.forEach { rows[it.id] = it } }
        override suspend fun updateStatusIf(id: String, status: String, allowedStatuses: List<String>): Int {
            val current = rows[id] ?: return 0
            return if (current.status in allowedStatuses) {
                rows[id] = current.copy(status = status)
                1
            } else 0
        }
        override suspend fun updateStatus(id: String, status: String): Int {
            val current = rows[id] ?: return 0
            rows[id] = current.copy(status = status)
            return 1
        }
        override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long): Int {
            val current = rows[id] ?: return 0
            rows[id] = current.copy(uploadedBytes = uploadedBytes, totalBytes = totalBytes, progress = progress, speed = speed, averageSpeed = averageSpeed, eta = eta)
            return 1
        }
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {
            update(id) { it.copy(uploadDurationMs = durationMs) }
        }
        override suspend fun updateMessageLink(id: String, messageLink: String) {
            update(id) { it.copy(messageLink = messageLink) }
        }
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {
            update(id) { it.copy(provisionalMessageId = messageId) }
        }
        override suspend fun markSendDispatched(id: String) {
            update(id) { it.copy(sendDispatched = true) }
        }
        override suspend fun clearSendDispatched(id: String) {
            update(id) { it.copy(sendDispatched = false) }
        }
        override suspend fun markSendConfirmed(id: String, messageId: Long, messageLink: String?) {
            update(id) {
                it.copy(finalMessageId = messageId, messageLink = messageLink, provisionalMessageId = null, sendDispatched = false)
            }
        }
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun deleteUploadById(id: String) { rows.remove(id) }
        override suspend fun deleteCompletedUploads() { rows.values.removeAll { it.status == "COMPLETED" } }
        override suspend fun clearAllUploads() { rows.clear() }
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