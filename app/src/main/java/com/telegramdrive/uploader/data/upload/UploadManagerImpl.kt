package com.telegramdrive.uploader.data.upload

import androidx.work.*
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.feature.upload.worker.UploadWorker
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadManagerImpl @Inject constructor(
    private val repository: UploadRepository,
    private val workManager: WorkManager
) : UploadManager {
    override fun enqueueUpload(task: UploadTask) {
        enqueueUpload(task, 0L)
    }

    override fun enqueueUpload(task: UploadTask, delayMs: Long) {
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

        workManager.enqueueUniqueWork(
            task.id,
            ExistingWorkPolicy.KEEP,
            uploadRequest
        )
        DiagnosticsManager.log(
            category = DiagnosticCategory.WORKER_ENQUEUED,
            severity = DiagnosticSeverity.INFO,
            message = "WorkManager accepted upload work request.",
            uploadId = task.id
        )
    }

    override fun pauseUpload(id: String) {
        workManager.cancelUniqueWork(id)
    }

    override fun resumeUpload(task: UploadTask) {
        enqueueUpload(task, task.scheduledAt?.let { (it - System.currentTimeMillis()).coerceAtLeast(0L) } ?: 0L)
    }

    override fun cancelUpload(id: String) {
        workManager.cancelUniqueWork(id)
    }

    override fun retryUpload(task: UploadTask) {
        enqueueUpload(task, 0L)
    }

    override fun observeUpload(id: String): Flow<UploadTask?> {
        return repository.observeUploadById(id)
    }

    override fun observeUploads(): Flow<List<UploadTask>> {
        return repository.getAllUploads()
    }
}
