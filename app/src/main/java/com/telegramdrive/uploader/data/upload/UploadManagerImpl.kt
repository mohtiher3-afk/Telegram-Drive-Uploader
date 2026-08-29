package com.telegramdrive.uploader.data.upload

import androidx.work.*
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.feature.upload.worker.UploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import com.google.common.util.concurrent.ListenableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManagerImpl @Inject constructor(
    private val repository: UploadRepository,
    private val workManager: WorkManager
) : UploadManager {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            reconcileInterruptedUploads()
        }
    }
    override fun enqueueUpload(task: UploadTask) {
        enqueueUpload(task, 0L, UploadWorkPolicy.existingWorkPolicy(false))
    }

    override fun enqueueUpload(task: UploadTask, delayMs: Long) {
        enqueueUpload(task, delayMs, UploadWorkPolicy.existingWorkPolicy(false))
    }

    private fun enqueueUpload(
        task: UploadTask,
        delayMs: Long,
        workPolicy: ExistingWorkPolicy
    ) {
        val inputData = Data.Builder()
            .putString("upload_id", task.id)
            .build()

        val constraints = UploadWorkPolicy.constraints()

        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(delayMs.coerceAtLeast(0L), java.util.concurrent.TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                java.util.concurrent.TimeUnit.SECONDS
            )
            .addTag("tdlib_uploads")
            .addTag("upload_${task.id}")
            .build()

        workManager.enqueueUniqueWork(task.id, workPolicy, uploadRequest)
        DiagnosticsManager.log(
            category = DiagnosticCategory.WORKER_ENQUEUED,
            severity = DiagnosticSeverity.INFO,
            message = "WorkManager accepted upload work request.",
            uploadId = task.id
        )
        // Read one asynchronous snapshot without observeForever, which could retain this manager.
        val stateFuture = workManager.getWorkInfosForUniqueWork(task.id)
        stateFuture.addListener({
            runCatching { stateFuture.get().firstOrNull() }
                .onSuccess { info ->
                    DiagnosticsManager.log(
                        category = DiagnosticCategory.WORKER_ENQUEUED,
                        severity = DiagnosticSeverity.INFO,
                        message = "WorkManager state after enqueue: ${info?.state ?: "UNKNOWN"}; runAttemptCount=${info?.runAttemptCount ?: -1}.",
                        uploadId = task.id
                    )
                }
                .onFailure { error ->
                    DiagnosticsManager.log(
                        category = DiagnosticCategory.WORKER_STOPPED,
                        severity = DiagnosticSeverity.ERROR,
                        message = "Unable to read WorkManager state after enqueue: ${error.message ?: "unknown error"}.",
                        uploadId = task.id
                    )
                }
        }, java.util.concurrent.Executor { it.run() })
    }

    override fun pauseUpload(id: String) {
        workManager.cancelUniqueWork(id)
    }

    override fun resumeUpload(task: UploadTask) {
        enqueueUpload(
            task,
            task.scheduledAt?.let { (it - System.currentTimeMillis()).coerceAtLeast(0L) } ?: 0L,
            UploadWorkPolicy.existingWorkPolicy(true)
        )
    }

    override fun cancelUpload(id: String) {
        workManager.cancelUniqueWork(id)
    }

    override fun retryUpload(task: UploadTask) {
        enqueueUpload(task, 0L, UploadWorkPolicy.existingWorkPolicy(true))
    }

    override suspend fun reconcileInterruptedUploads(): Int {
        val interruptedUploads = repository.getInterruptedUploads()
        var reconciledCount = 0
        
        for (task in interruptedUploads) {
            // Check if there is an active worker for this task
            val workInfos = workManager.getWorkInfosForUniqueWork(task.id).await()
            val isTaskActive = workInfos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            
            if (!isTaskActive) {
                repository.updateStatus(task.id, UploadStatus.QUEUED)
                enqueueUpload(task)
                reconciledCount++
            }
        }
        
        if (reconciledCount > 0) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.APP_START,
                severity = DiagnosticSeverity.INFO,
                message = "Reconciled $reconciledCount interrupted upload tasks back to queued state."
            )
        }
        return reconciledCount
    }

    private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addListener({
            try {
                continuation.resume(get())
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, Dispatchers.IO.asExecutor())
        
        continuation.invokeOnCancellation { cancel(true) }
    }

    override fun observeUpload(id: String): Flow<UploadTask?> {
        return repository.observeUploadById(id)
    }

    override fun observeUploads(): Flow<List<UploadTask>> {
        return repository.getAllUploads()
    }
}
