package com.telegramdrive.uploader.data.upload

import android.net.Uri
import android.os.SystemClock
import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.data.telegram.client.TelegramUploadEvent
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.SpeedCalculator
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramUploadEngineImpl @Inject constructor(
    private val streamingFileReader: StreamingFileReader,
    private val telegramClient: TelegramClient
) : TelegramUploadEngine {

    override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
        if (!telegramClient.isConfigured) {
            emit(UploadEngineResult.Error("Telegram TDLib credentials are not configured", false))
            return@flow
        }
        if (telegramClient.connectionState.value.name != "AUTHORIZED") {
            emit(UploadEngineResult.Error("Telegram account is not authorized", true))
            return@flow
        }
        if (task.destinationId == 0L) {
            emit(UploadEngineResult.Error("A Telegram destination is required", false))
            return@flow
        }

        val source = Uri.parse(task.sourceUri)
        val stagedFile = File.createTempFile("tdlib-upload-", "-${safeName(task.fileName)}")
        val speedCalculator = SpeedCalculator()
        try {
            val copiedBytes = streamingFileReader.copyToFile(source, stagedFile)
            val totalBytes = copiedBytes.takeIf { it > 0L } ?: task.fileSize
            if (totalBytes <= 0L) {
                emit(UploadEngineResult.Error("Unable to determine source file size", false))
                return@flow
            }

            emit(progress(0L, totalBytes, speedCalculator))
            val uploadStartedAt = SystemClock.elapsedRealtime()
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_STARTED,
                severity = DiagnosticSeverity.INFO,
                message = "Staged source is ready; handing off to TDLib upload.",
                uploadId = task.id
            )
            telegramClient.uploadLocalDocument(task, stagedFile.absolutePath).collect { event ->
                when (event) {
                    is TelegramUploadEvent.Progress -> {
                        val uploaded = event.uploadedBytes.coerceIn(0L, totalBytes)
                        emit(progress(uploaded, totalBytes, speedCalculator))
                    }
                    TelegramUploadEvent.Completed -> emit(
                        UploadEngineResult.Success(
                            uploadDurationMs = (SystemClock.elapsedRealtime() - uploadStartedAt).coerceAtLeast(0L)
                        )
                    )
                    is TelegramUploadEvent.Failed -> emit(
                        UploadEngineResult.Error(event.message, event.retryable)
                    )
                }
            }
        } catch (error: Throwable) {
            emit(UploadEngineResult.Error(error.message ?: "TDLib upload failed", isRetryable(error)))
        } finally {
            stagedFile.delete()
        }
    }

    private fun progress(
        uploadedBytes: Long,
        totalBytes: Long,
        speedCalculator: SpeedCalculator
    ): UploadEngineResult.Progress {
        val speed = speedCalculator.update(uploadedBytes)
        val percentage = (uploadedBytes * 100f / totalBytes).coerceIn(0f, 100f)
        val eta = if (speed.currentSpeed > 0) {
            (totalBytes - uploadedBytes) / speed.currentSpeed
        } else 0L
        return UploadEngineResult.Progress(
            UploadProgress(
                uploadedBytes = uploadedBytes,
                totalBytes = totalBytes,
                percentage = percentage,
                speedBytesPerSecond = speed.currentSpeed,
                averageSpeedBytesPerSecond = speed.averageSpeed,
                etaSeconds = eta
            )
        )
    }

    private fun safeName(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "file" }

    private fun isRetryable(error: Throwable): Boolean =
        when (error) {
            is java.io.FileNotFoundException -> false
            is java.io.IOException -> true
            is java.net.SocketException -> true
            is java.net.UnknownHostException -> true
            is java.util.concurrent.TimeoutException -> true
            else -> false
        }
}
