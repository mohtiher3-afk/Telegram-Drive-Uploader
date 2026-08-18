package com.telegramdrive.uploader.feature.upload.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.collectLatest

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: UploadRepository,
    private val uploadEngine: TelegramUploadEngine
) : CoroutineWorker(context, params) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "upload_service_channel"

    init {
        createNotificationChannel()
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

        // Promote worker to foreground service
        try {
            val initialForegroundInfo = createForegroundInfo(uploadTask, progressPercent = 0, speedFormatted = "Preparing...")
            setForeground(initialForegroundInfo)
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.WORKER_STARTED,
                severity = DiagnosticSeverity.WARN,
                message = "Could not set worker to foreground: ${e.message}",
                uploadId = uploadId
            )
        }

        repository.updateStatus(uploadId, UploadStatus.PREPARING)
        DiagnosticsManager.log(
            category = DiagnosticCategory.UPLOAD_STARTED,
            severity = DiagnosticSeverity.INFO,
            message = "Upload task state transitioned to PREPARING.",
            uploadId = uploadId
        )

        var result: Result = Result.success()
        val startTime = System.currentTimeMillis()
        var lastNotificationTime = 0L

        try {
            uploadEngine.uploadFile(uploadTask).collectLatest { engineResult ->
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

                        val now = System.currentTimeMillis()
                        if (now - lastNotificationTime > 800) {
                            lastNotificationTime = now
                            val speedFormatted = formatSpeed(p.speedBytesPerSecond)
                            val etaFormatted = formatEta(p.etaSeconds)
                            val infoText = "$speedFormatted • ETA: $etaFormatted"
                            val notifInfo = createForegroundInfo(uploadTask, p.percentage.toInt(), infoText)
                            setForeground(notifInfo)
                        }
                    }
                    is UploadEngineResult.Success -> {
                        repository.updateStatus(uploadId, UploadStatus.COMPLETED)
                        result = Result.success()
                        val duration = System.currentTimeMillis() - startTime
                        showCompletionNotification(uploadTask, isSuccess = true)
                        DiagnosticsManager.log(
                            category = DiagnosticCategory.UPLOAD_COMPLETED,
                            severity = DiagnosticSeverity.INFO,
                            message = "Upload task completed successfully.",
                            uploadId = uploadId,
                            durationMs = duration
                        )
                    }
                    is UploadEngineResult.Error -> {
                        repository.updateStatus(uploadId, UploadStatus.FAILED)
                        showCompletionNotification(uploadTask, isSuccess = false, errorMessage = engineResult.message)
                        result = if (engineResult.isRetryable) {
                            DiagnosticsManager.log(
                                category = DiagnosticCategory.UPLOAD_RETRY,
                                severity = DiagnosticSeverity.WARN,
                                message = "Upload task failed with a transient error. Scheduled for retry.",
                                uploadId = uploadId,
                                errorCode = ErrorCode.UPLOAD_FAILED
                            )
                            Result.retry()
                        } else {
                            DiagnosticsManager.log(
                                category = DiagnosticCategory.UPLOAD_FAILED,
                                severity = DiagnosticSeverity.ERROR,
                                message = "Upload task failed with a non-recoverable error: ${engineResult.message}.",
                                uploadId = uploadId,
                                errorCode = ErrorCode.UPLOAD_FAILED
                            )
                            Result.failure()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            repository.updateStatus(uploadId, UploadStatus.FAILED)
            showCompletionNotification(uploadTask, isSuccess = false, errorMessage = e.message)
            result = Result.failure()
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

        DiagnosticsManager.log(
            category = DiagnosticCategory.WORKER_STOPPED,
            severity = DiagnosticSeverity.INFO,
            message = "Background upload worker has finished execution with status: $result",
            uploadId = uploadId
        )

        return result
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Telegram Uploader Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live upload progress and speeds for Telegram transfers"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(task: UploadTask, progressPercent: Int, speedFormatted: String): ForegroundInfo {
        val notificationId = task.id.hashCode()
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Uploading ${task.fileName}")
            .setContentText(speedFormatted)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setProgress(100, progressPercent.coerceIn(0, 100), progressPercent <= 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    private fun showCompletionNotification(task: UploadTask, isSuccess: Boolean, errorMessage: String? = null) {
        val notificationId = task.id.hashCode()
        val title = if (isSuccess) "Upload Complete" else "Upload Failed"
        val text = if (isSuccess) "${task.fileName} uploaded successfully to Telegram" else "${task.fileName}: ${errorMessage ?: "Transfer failed"}"
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (_: Exception) {}
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec / (1024.0 * 1024.0))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024.0)
            else -> "$bytesPerSec B/s"
        }
    }

    private fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "--"
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }
}
