package com.telegramdrive.uploader.data.upload

import android.content.Context
import android.net.Uri
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.data.telegram.client.TdLibClient
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.SpeedCalculator
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramUploadEngineImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamingFileReader: StreamingFileReader,
    private val tdLibClient: TdLibClient?
) : TelegramUploadEngine {

    override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
        val speedCalculator = SpeedCalculator()
        val uri = Uri.parse(task.sourceUri)

        DiagnosticsManager.log(
            category = DiagnosticCategory.UPLOAD_STARTED,
            severity = DiagnosticSeverity.INFO,
            message = "Starting file upload for task ${task.id} (${task.fileName}, ${task.fileSize} bytes) to chat ${task.destinationId}.",
            uploadId = task.id
        )

        val fileSize = try {
            streamingFileReader.getFileSize(uri)
        } catch (_: Exception) {
            task.fileSize
        }
        val totalBytes = if (fileSize > 0) fileSize else task.fileSize

        emit(UploadEngineResult.Progress(
            UploadProgress(
                uploadedBytes = 0L,
                totalBytes = totalBytes,
                percentage = 0f,
                speedBytesPerSecond = 0L,
                averageSpeedBytesPerSecond = 0L,
                etaSeconds = 0L
            )
        ))

        val client = tdLibClient
        if (client == null || client.authorizationState.value !is TdLibClient.AuthState.Ready) {
            val errorMsg = "TDLIB_NATIVE_UNAVAILABLE: Telegram runtime is not ready or authenticated. Cannot send file."
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = errorMsg,
                uploadId = task.id,
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            emit(UploadEngineResult.Error(errorMsg, false))
            return@flow
        }

        var stagedFile: File? = null
        try {
            stagedFile = stageFileForUpload(task, uri)
            if (stagedFile == null || !stagedFile.exists()) {
                val errorMsg = "Could not stage local file for TDLib transfer."
                DiagnosticsManager.log(
                    category = DiagnosticCategory.UPLOAD_FAILED,
                    severity = DiagnosticSeverity.ERROR,
                    message = errorMsg,
                    uploadId = task.id,
                    errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE
                )
                emit(UploadEngineResult.Error(errorMsg, false))
                return@flow
            }

            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_STARTED,
                severity = DiagnosticSeverity.INFO,
                message = "Submitting staged file (${stagedFile.length()} bytes) to TDLib SendMessage API.",
                uploadId = task.id
            )

            val isVideo = task.mimeType.startsWith("video/")
            val messageResult = client.sendDocumentOrVideoMessage(
                chatId = task.destinationId,
                localFilePath = stagedFile.absolutePath,
                mimeType = task.mimeType,
                caption = task.fileName,
                isVideo = isVideo,
                width = task.width ?: 0,
                height = task.height ?: 0,
                duration = task.duration?.toInt() ?: 0
            )

            if (messageResult == null) {
                val errorMsg = "TDLib SendMessage operation failed or returned null response."
                DiagnosticsManager.log(
                    category = DiagnosticCategory.UPLOAD_FAILED,
                    severity = DiagnosticSeverity.ERROR,
                    message = errorMsg,
                    uploadId = task.id,
                    errorCode = ErrorCode.UPLOAD_FAILED
                )
                emit(UploadEngineResult.Error(errorMsg, true))
                return@flow
            }

            val speedInfo = speedCalculator.update(totalBytes)
            emit(UploadEngineResult.Progress(
                UploadProgress(
                    uploadedBytes = totalBytes,
                    totalBytes = totalBytes,
                    percentage = 100f,
                    speedBytesPerSecond = speedInfo.currentSpeed,
                    averageSpeedBytesPerSecond = speedInfo.averageSpeed,
                    etaSeconds = 0L
                )
            ))

            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_COMPLETED,
                severity = DiagnosticSeverity.INFO,
                message = "File upload confirmed by TDLib SendMessage (Message ID: ${messageResult.id}) for task ${task.id}.",
                uploadId = task.id
            )
            emit(UploadEngineResult.Success)

        } catch (e: IOException) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.WARN,
                message = "I/O exception during transfer: ${e.message}",
                uploadId = task.id,
                errorCode = ErrorCode.UPLOAD_FAILED,
                exception = e
            )
            emit(UploadEngineResult.Error("Network error during transfer: ${e.localizedMessage ?: "I/O error"}", true))
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = "Upload failed: ${e.message}",
                uploadId = task.id,
                errorCode = ErrorCode.UPLOAD_FAILED,
                exception = e
            )
            emit(UploadEngineResult.Error("Upload failed: ${e.localizedMessage ?: "Unknown error"}", false))
        } finally {
            try {
                stagedFile?.delete()
            } catch (_: Exception) {}
        }
    }

    private suspend fun stageFileForUpload(task: UploadTask, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val stagingDir = File(context.cacheDir, "upload_staging").apply { mkdirs() }
            val cleanName = task.fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val targetFile = File(stagingDir, "${task.id}_$cleanName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.WARN,
                message = "Could not stage local file for TDLib: ${e.message}",
                uploadId = task.id,
                errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE
            )
            null
        }
    }
}
