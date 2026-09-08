package com.telegramdrive.uploader.data.upload

import android.net.Uri
import android.os.SystemClock
import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.data.telegram.client.TelegramUploadEvent
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.SpeedCalculator
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramUploadEngineImpl @Inject constructor(
    private val streamingFileReader: StreamingFileReader,
    private val telegramClient: TelegramClient
) : TelegramUploadEngine {

    companion object {
        private const val AUTH_WAIT_TIMEOUT_MS = 30_000L
    }

    override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
        if (!telegramClient.isConfigured) {
            emit(UploadEngineResult.Error("Telegram TDLib credentials are not configured", false))
            return@flow
        }
        // Wait (bounded) for the TDLib session to reach AUTHORIZED. Background workers can
        // be resumed by WorkManager before the client finishes bootstrapping at cold start;
        // failing instantly on "not authorized yet" would burn a retry attempt on every
        // queued task at once. Instead wait up to AUTH_WAIT_TIMEOUT_MS, then fail
        // non-retryable on timeout or a terminal/error state so the loop cannot spin forever.
        val authState = try {
            withTimeout(AUTH_WAIT_TIMEOUT_MS) {
                telegramClient.connectionState.first { state ->
                    state != TelegramConnectionState.DISCONNECTED &&
                        state != TelegramConnectionState.CONNECTING
                }
            }
        } catch (e: TimeoutCancellationException) {
            emit(
                UploadEngineResult.Error(
                    "Timed out waiting for Telegram authorization; re-authenticate to continue.",
                    false
                )
            )
            return@flow
        }
        if (authState != TelegramConnectionState.AUTHORIZED) {
            emit(
                UploadEngineResult.Error(
                    "Telegram account is not authorized (state: ${authState}); re-authenticate to continue.",
                    false
                )
            )
            return@flow
        }
        if (task.destinationId == 0L) {
            emit(UploadEngineResult.Error("A Telegram destination is required", false))
            return@flow
        }

        val source = Uri.parse(task.sourceUri)
        val speedCalculator = SpeedCalculator()
        var stagedFile: File? = null
        try {
            // Fast path: a readable file:// source is handed straight to TDLib, skipping a
            // full read+write staging copy (roughly halves time-to-first-byte for on-disk
            // files). content:// sources and file:// paths the process cannot read fall back
            // to staging, so scoped-storage grants keep working via the content resolver.
            val directPath: String? = if (source.scheme == "file") {
                source.path?.let { p -> File(p).takeIf { it.isFile && it.canRead() }?.absolutePath }
            } else {
                null
            }

            val localPath: String
            val totalBytes: Long
            if (directPath != null) {
                localPath = directPath
                totalBytes = File(directPath).length().takeIf { it > 0L } ?: task.fileSize
            } else {
                val tmp = File.createTempFile("tdlib-upload-", "-${safeName(task.fileName)}")
                stagedFile = tmp
                // copyToFile is a blocking full-file read/write; keep it off the worker's
                // compute dispatcher so the upload coroutine does not tie up a shared thread.
                val copiedBytes = withContext(Dispatchers.IO) {
                    streamingFileReader.copyToFile(source, tmp)
                }
                localPath = tmp.absolutePath
                totalBytes = copiedBytes.takeIf { it > 0L } ?: task.fileSize
            }

            if (totalBytes <= 0L) {
                emit(UploadEngineResult.Error("Unable to determine source file size", false))
                return@flow
            }

            emit(progress(0L, totalBytes, speedCalculator))
            val uploadStartedAt = SystemClock.elapsedRealtime()
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_STARTED,
                severity = DiagnosticSeverity.INFO,
                message = if (directPath != null) {
                    "Readable file source handed off to TDLib without staging."
                } else {
                    "Staged source is ready; handing off to TDLib upload."
                },
                uploadId = task.id
            )
            telegramClient.uploadLocalDocument(task, localPath).collect { event ->
                when (event) {
                    is TelegramUploadEvent.Progress -> {
                        val uploaded = event.uploadedBytes.coerceIn(0L, totalBytes)
                        emit(progress(uploaded, totalBytes, speedCalculator))
                    }
                    is TelegramUploadEvent.Completed -> emit(
                        UploadEngineResult.Success(
                            uploadDurationMs = (SystemClock.elapsedRealtime() - uploadStartedAt).coerceAtLeast(0L),
                            messageLink = event.messageLink
                        )
                    )
                    is TelegramUploadEvent.Failed -> emit(
                        UploadEngineResult.Error(event.message, event.retryable)
                    )
                }
            }
        } catch (error: Throwable) {
            // Surface the real cause at the point it is thrown. Background workers retry
            // transient failures without logging the reason (see UploadWorker), so without
            // this the diagnostics export never shows why a retrying task actually failed.
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = "Upload engine failed (retryable=${isRetryable(error)}).",
                uploadId = task.id,
                errorCode = DiagnosticsManager.mapExceptionToCode(error),
                exception = error
            )
            emit(UploadEngineResult.Error(error.message ?: "TDLib upload failed", isRetryable(error)))
        } finally {
            stagedFile?.delete()
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
