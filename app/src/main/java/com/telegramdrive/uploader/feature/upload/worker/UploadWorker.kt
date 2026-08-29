package com.telegramdrive.uploader.feature.upload.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadCompletionPolicy
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import com.telegramdrive.uploader.domain.upload.UploadEventNotifier
import com.telegramdrive.uploader.domain.upload.UploadEventNotificationPolicy
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collect

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UploadRepository,
    private val uploadEngine: TelegramUploadEngine,
    private val uploadEventNotifier: UploadEventNotifier
) : CoroutineWorker(context, params) {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
    }

    override suspend fun doWork(): Result {
        val uploadId = inputData.getString("upload_id") ?: return Result.failure()
        
        DiagnosticsManager.log(
            category = DiagnosticCategory.WORKER_STARTED,
            severity = DiagnosticSeverity.INFO,
            message = "Background upload worker has started execution.",
            uploadId = uploadId
        )
        
        val uploadTask = repository.getUploadById(uploadId) ?: run {
            DiagnosticsManager.log(
                category = DiagnosticCategory.WORKER_STOPPED,
                severity = DiagnosticSeverity.ERROR,
                message = "Background upload worker aborted: upload task not found in database.",
                uploadId = uploadId,
                errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE
            )
            return Result.failure()
        }

        if (uploadTask.status == UploadStatus.COMPLETED) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.WORKER_STOPPED,
                severity = DiagnosticSeverity.INFO,
                message = "Background upload worker skipped execution: upload task is already completed.",
                uploadId = uploadId
            )
            return Result.success()
        }

        if (uploadTask.status == UploadStatus.CANCELLED || uploadTask.status == UploadStatus.PAUSED) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.WORKER_STOPPED,
                severity = DiagnosticSeverity.INFO,
                message = "Background upload worker skipped execution: upload task is ${uploadTask.status.name.lowercase()}.",
                uploadId = uploadId
            )
            return Result.success()
        }

        repository.updateStatus(uploadId, UploadStatus.PREPARING)
        DiagnosticsManager.log(
            category = DiagnosticCategory.UPLOAD_PREPARING,
            severity = DiagnosticSeverity.INFO,
            message = "Upload task entered preflight; waiting for TDLib handoff.",
            uploadId = uploadId
        )

        var result: Result = Result.failure()
        var terminalEventReceived = false
        val startTime = System.currentTimeMillis()

        try {
            uploadEngine.uploadFile(uploadTask).collect { engineResult ->
                if (isStopped) {
                    return@collect
                }
                when (engineResult) {
                    is UploadEngineResult.Progress -> {
                        val p = engineResult.progress
                        repository.updateProgress(
                            id = uploadId,
                            uploadedBytes = p.uploadedBytes,
                            totalBytes = p.totalBytes,
                            progress = p.percentage,
                            speed = p.speedBytesPerSecond,
                            averageSpeed = p.averageSpeedBytesPerSecond,
                            eta = p.etaSeconds
                        )
                        // Do not log every progress event at high frequency in production to preserve resource usage.
                    }
                    is UploadEngineResult.Success -> {
                        terminalEventReceived = true
                        val latestTask = repository.getUploadById(uploadId)
                        if (latestTask?.status != UploadStatus.CANCELLED && latestTask?.status != UploadStatus.PAUSED) {
                            repository.updateUploadDuration(uploadId, engineResult.uploadDurationMs)
                            repository.updateStatus(uploadId, UploadStatus.COMPLETED)
                            notifyTerminalStatus(uploadId, UploadStatus.COMPLETED)
                            result = Result.success()
                            val duration = System.currentTimeMillis() - startTime
                            DiagnosticsManager.log(
                                category = DiagnosticCategory.UPLOAD_COMPLETED,
                                severity = DiagnosticSeverity.INFO,
                                message = "Upload task completed successfully.",
                                uploadId = uploadId,
                                durationMs = duration
                            )
                        } else {
                            result = Result.success()
                        }
                    }
                    is UploadEngineResult.Error -> {
                        terminalEventReceived = true
                        val latestTask = repository.getUploadById(uploadId)
                        if (latestTask?.status == UploadStatus.CANCELLED || latestTask?.status == UploadStatus.PAUSED) {
                            result = Result.success()
                        } else {
                            val canRetry = engineResult.isRetryable && runAttemptCount < MAX_RETRY_ATTEMPTS
                            repository.updateStatus(
                                uploadId,
                                if (canRetry) UploadStatus.RETRYING else UploadStatus.FAILED
                            )
                            result = if (canRetry) {
                                DiagnosticsManager.log(
                                    category = DiagnosticCategory.UPLOAD_RETRY,
                                    severity = DiagnosticSeverity.WARN,
                                    message = "Upload task failed transiently (${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS). WorkManager will retry it.",
                                    uploadId = uploadId,
                                    errorCode = ErrorCode.UPLOAD_FAILED
                                )
                                Result.retry()
                            } else {
                                notifyTerminalStatus(uploadId, UploadStatus.FAILED)
                                DiagnosticsManager.log(
                                    category = DiagnosticCategory.UPLOAD_FAILED,
                                    severity = DiagnosticSeverity.ERROR,
                                    message = "Upload task failed permanently after ${runAttemptCount + 1} attempts: ${engineResult.message}.",
                                    uploadId = uploadId,
                                    errorCode = ErrorCode.UPLOAD_FAILED
                                )
                                Result.failure()
                            }
                        }
                    }
                }
            }
            if (isStopped) {
                val latestTask = repository.getUploadById(uploadId)
                return if (latestTask?.status == UploadStatus.CANCELLED || latestTask?.status == UploadStatus.PAUSED) {
                    Result.success()
                } else {
                    repository.updateStatus(uploadId, UploadStatus.RETRYING)
                    Result.retry()
                }
            }
            if (UploadCompletionPolicy.decide(terminalEventReceived) == UploadCompletionPolicy.Decision.UNCONFIRMED) {
                val latestTask = repository.getUploadById(uploadId)
                if (latestTask?.status != UploadStatus.CANCELLED && latestTask?.status != UploadStatus.PAUSED) {
                    repository.updateStatus(uploadId, UploadStatus.FAILED)
                    notifyTerminalStatus(uploadId, UploadStatus.FAILED)
                    DiagnosticsManager.log(
                        category = DiagnosticCategory.UPLOAD_FAILED,
                        severity = DiagnosticSeverity.ERROR,
                        message = "TDLib upload stream ended without confirmed Telegram delivery.",
                        uploadId = uploadId,
                        errorCode = ErrorCode.UPLOAD_FAILED
                    )
                    result = Result.failure()
                } else {
                    result = Result.success()
                }
            }
        } catch (e: Exception) {
            val latestTask = repository.getUploadById(uploadId)
            if (latestTask?.status == UploadStatus.CANCELLED || latestTask?.status == UploadStatus.PAUSED || isStopped) {
                result = Result.success()
            } else {
                val canRetry = runAttemptCount < MAX_RETRY_ATTEMPTS
                repository.updateStatus(
                    uploadId,
                    if (canRetry) UploadStatus.RETRYING else UploadStatus.FAILED
                )
                if (!canRetry) notifyTerminalStatus(uploadId, UploadStatus.FAILED)
                result = if (canRetry) Result.retry() else Result.failure()
                val mappedCategory = DiagnosticsManager.mapException(e)
                val mappedCode = DiagnosticsManager.mapExceptionToCode(e)
                DiagnosticsManager.log(
                    category = DiagnosticCategory.UPLOAD_FAILED,
                    severity = DiagnosticSeverity.ERROR,
                    message = "Upload worker crashed due to an unhandled exception.",
                    uploadId = uploadId,
                    errorCode = mappedCode,
                    exception = e
                )
            }
        }

        DiagnosticsManager.log(
            category = DiagnosticCategory.WORKER_STOPPED,
            severity = DiagnosticSeverity.INFO,
            message = "Background upload worker has finished execution with status: $result",
            uploadId = uploadId
        )

        return result
    }

    private fun notifyTerminalStatus(uploadId: String, status: UploadStatus) {
        UploadEventNotificationPolicy.eventFor(status)?.let { event ->
            uploadEventNotifier.notify(event, uploadId)
        }
    }
}
