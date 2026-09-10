package com.telegramdrive.uploader.feature.upload.worker

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import com.telegramdrive.uploader.domain.upload.UploadEventNotificationEvent
import com.telegramdrive.uploader.domain.upload.UploadEventNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UploadWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repository: FakeRepository
    private lateinit var engine: FakeEngine
    private lateinit var notifier: FakeNotifier

    @Before
    fun setUp() {
        DiagnosticsManager.clearDiagnostics()
        notifier = FakeNotifier(context)
        engine = FakeEngine()
    }

    private fun newTask(status: UploadStatus) = UploadTask(
        id = UPLOAD_ID,
        sourceUri = "file:///sdcard/clip.mp4",
        fileName = "clip.mp4",
        fileSize = 2048L,
        destinationId = 7L,
        status = status
    )

    private fun queuedRepository() {
        repository = FakeRepository(newTask(UploadStatus.QUEUED))
    }

    private fun buildWorker(attempt: Int = 0): UploadWorker {
        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = UploadWorker(appContext, workerParameters, repository, engine, notifier)
        }
        return TestListenableWorkerBuilder.from(context, UploadWorker::class.java)
            .setInputData(workDataOf("upload_id" to UPLOAD_ID))
            .setRunAttemptCount(attempt)
            .setWorkerFactory(factory)
            .build()
    }

    @Test
    fun `success completes the task and records delivery metadata`() = runTest {
        queuedRepository()
        engine.results = listOf(progress(100f), UploadEngineResult.Success(uploadDurationMs = 1234L, messageLink = "https://t.me/c/1/9"))

        val result = buildWorker().doWork()

        assertTrue("Expected success, got $result", result is ListenableWorker.Result.Success)
        assertEquals(UploadStatus.COMPLETED, repository.task?.status)
        assertEquals("https://t.me/c/1/9", repository.lastMessageLink)
        assertEquals(1234L, repository.lastDurationMs)
        assertEquals(listOf(UploadStatus.PREPARING, UploadStatus.COMPLETED), repository.statusTransitions)
        assertTrue(UploadEventNotificationEvent.COMPLETED in notifier.notified)
    }

    @Test
    fun `retryable error below the limit schedules a retry`() = runTest {
        queuedRepository()
        engine.results = listOf(UploadEngineResult.Error("network blip", isRetryable = true))

        val result = buildWorker().doWork()

        assertTrue("Expected retry, got $result", result is ListenableWorker.Result.Retry)
        assertEquals(UploadStatus.RETRYING, repository.task?.status)
        assertEquals(listOf(UploadStatus.PREPARING, UploadStatus.RETRYING), repository.statusTransitions)
    }

    @Test
    fun `non-retryable error fails permanently`() = runTest {
        queuedRepository()
        engine.results = listOf(UploadEngineResult.Error("source gone", isRetryable = false))

        val result = buildWorker().doWork()

        assertTrue("Expected failure, got $result", result is ListenableWorker.Result.Failure)
        assertEquals(UploadStatus.FAILED, repository.task?.status)
        assertTrue(UploadEventNotificationEvent.FAILED in notifier.notified)
    }

    @Test
    fun `retryable error at the attempt limit fails instead of retrying`() = runTest {
        queuedRepository()
        engine.results = listOf(UploadEngineResult.Error("still failing", isRetryable = true))

        val result = buildWorker(attempt = 5).doWork()

        assertTrue("Expected failure at attempt limit, got $result", result is ListenableWorker.Result.Failure)
        assertEquals(UploadStatus.FAILED, repository.task?.status)
    }

    @Test
    fun `already completed task is skipped without invoking the engine`() = runTest {
        repository = FakeRepository(newTask(UploadStatus.COMPLETED))

        val result = buildWorker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(0, engine.calls)
    }

    @Test
    fun `cancelled task is skipped without invoking the engine`() = runTest {
        repository = FakeRepository(newTask(UploadStatus.CANCELLED))

        val result = buildWorker().doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(0, engine.calls)
    }

    @Test
    fun `missing task fails immediately`() = runTest {
        repository = FakeRepository(null)

        val result = buildWorker().doWork()

        assertTrue("Expected failure for missing task, got $result", result is ListenableWorker.Result.Failure)
        assertEquals(0, engine.calls)
    }

    @Test
    fun `stream ending without a terminal event is treated as unconfirmed failure`() = runTest {
        queuedRepository()
        engine.results = listOf(progress(100f))

        val result = buildWorker().doWork()

        assertTrue("Expected failure for unconfirmed delivery, got $result", result is ListenableWorker.Result.Failure)
        assertEquals(UploadStatus.FAILED, repository.task?.status)
    }

    @Test
    fun `cancellation observed mid-flight returns success and does not mark failure`() = runTest {
        queuedRepository()
        engine.results = listOf(UploadEngineResult.Error("aborted", isRetryable = true))
        engine.onCollect = { repository.task = repository.task?.copy(status = UploadStatus.CANCELLED) }

        val result = buildWorker().doWork()

        assertTrue("Expected success after mid-flight cancel, got $result", result is ListenableWorker.Result.Success)
        assertEquals(UploadStatus.CANCELLED, repository.task?.status)
        assertTrue(
            "Must not record FAILED/RETRYING",
            UploadStatus.FAILED !in repository.statusTransitions && UploadStatus.RETRYING !in repository.statusTransitions
        )
    }

    private fun progress(percentage: Float) = UploadEngineResult.Progress(
        UploadProgress(
            uploadedBytes = 2048L,
            totalBytes = 2048L,
            percentage = percentage,
            speedBytesPerSecond = 1024L,
            averageSpeedBytesPerSecond = 1024L,
            etaSeconds = 0L
        )
    )

    private class FakeEngine : TelegramUploadEngine {
        var results: List<UploadEngineResult> = emptyList()
        var onCollect: (() -> Unit)? = null
        var calls = 0
        var cancelCalls = 0
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            calls++
            onCollect?.invoke()
            results.forEach { emit(it) }
        }
        override fun cancelActiveUploads() {
            cancelCalls++
        }
    }

    private class FakeRepository(initial: UploadTask?) : UploadRepository {
        var task: UploadTask? = initial
        val statusTransitions = mutableListOf<UploadStatus>()
        var lastMessageLink: String? = null
        var lastDurationMs: Long? = null
        var progressUpdates = 0

        override fun getAllUploads(): Flow<List<UploadTask>> = flowOf(listOfNotNull(task))
        override fun getActiveUploads(): Flow<List<UploadTask>> = flowOf(listOfNotNull(task))
        override suspend fun getUploadById(id: String): UploadTask? = task
        override fun observeUploadById(id: String): Flow<UploadTask?> = flowOf(task)
        override suspend fun insertUpload(upload: UploadTask) { task = upload }
        override suspend fun updateStatus(id: String, status: UploadStatus) {
            statusTransitions += status
            task = task?.copy(status = status)
        }
        override suspend fun updateStatusIf(id: String, status: UploadStatus, allowedStatuses: List<UploadStatus>) {
            if (task?.status in allowedStatuses) updateStatus(id, status)
        }
        override suspend fun updateProgress(id: String, uploadedBytes: Long, totalBytes: Long, progress: Float, speed: Long, averageSpeed: Long, eta: Long) {
            progressUpdates++
            task = task?.copy(uploadedBytes = uploadedBytes, totalBytes = totalBytes, progress = progress, speed = speed, averageSpeed = averageSpeed, eta = eta)
        }
        override suspend fun updateUploadDuration(id: String, durationMs: Long) {
            lastDurationMs = durationMs
            task = task?.copy(uploadDurationMs = durationMs)
        }
        override suspend fun updateMessageLink(id: String, messageLink: String) {
            lastMessageLink = messageLink
            task = task?.copy(messageLink = messageLink)
        }
        override suspend fun updateProvisionalMessageId(id: String, messageId: Long) {
            task = task?.copy(provisionalMessageId = messageId)
        }
        override suspend fun reconcileInterruptedUploads(): Int = 0
        override suspend fun getInterruptedUploads(): List<UploadTask> = emptyList()
        override suspend fun deleteUploadById(id: String) { task = null }
        override suspend fun deleteCompletedUploads() {}
        override suspend fun clearAllUploads() { task = null }
    }

    private class FakeNotifier(private val context: Context) : UploadEventNotifier {
        val notified = mutableListOf<UploadEventNotificationEvent>()
        var progressShown = 0
        var dismissed = 0
        override fun notify(event: UploadEventNotificationEvent, uploadId: String) { notified += event }
        override fun showProgressNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long) { progressShown++ }
        override fun dismissProgressNotification(uploadId: String) { dismissed++ }
        override fun buildForegroundNotification(uploadId: String, fileName: String, progress: Int, uploadedBytes: Long, totalBytes: Long): Notification =
            NotificationCompat.Builder(context, "upload-test").setContentTitle(fileName).setSmallIcon(android.R.drawable.stat_sys_upload).build()
    }

    private companion object {
        const val UPLOAD_ID = "upload-1"
    }
}
