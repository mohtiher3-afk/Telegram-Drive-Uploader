package com.telegramdrive.uploader.data.upload

import android.net.Uri
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReader
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.SpeedCalculator
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramUploadEngineImpl @Inject constructor(
    private val streamingFileReader: StreamingFileReader
) : TelegramUploadEngine {

    companion object {
        private const val CHUNK_SIZE_BYTES = 4 * 1024 * 1024
        private const val PROGRESS_EMIT_INTERVAL_MS = 250L
    }

    override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
        val speedCalculator = SpeedCalculator()
        val uri = Uri.parse(task.sourceUri)
        val fileSize = try {
            streamingFileReader.getFileSize(uri)
        } catch (e: Exception) {
            task.fileSize
        }
        val totalBytes = fileSize.takeIf { it > 0 } ?: task.fileSize
        if (totalBytes <= 0L) {
            emit(UploadEngineResult.Error("Unable to determine source file size", false))
            return@flow
        }

        var uploadedBytes = task.uploadedBytes.coerceIn(0L, totalBytes)
        var lastProgressEmitAt = 0L
        emit(emitProgress(uploadedBytes, totalBytes, speedCalculator))

        try {
            while (uploadedBytes < totalBytes) {
                val remaining = totalBytes - uploadedBytes
                val currentChunkSize = minOf(CHUNK_SIZE_BYTES.toLong(), remaining).toInt()
                val chunk = streamingFileReader.readChunk(uri, uploadedBytes, currentChunkSize)
                if (chunk.isEmpty()) {
                    emit(UploadEngineResult.Error("Source stream ended before all bytes were read", true))
                    return@flow
                }
                uploadedBytes += chunk.size.toLong()
                val now = System.currentTimeMillis()
                val shouldEmit = now - lastProgressEmitAt >= PROGRESS_EMIT_INTERVAL_MS || uploadedBytes >= totalBytes
                if (shouldEmit) {
                    emit(emitProgress(uploadedBytes, totalBytes, speedCalculator))
                    lastProgressEmitAt = now
                } else {
                    speedCalculator.update(uploadedBytes)
                }
            }
            emit(UploadEngineResult.Success)
        } catch (e: IOException) {
            emit(UploadEngineResult.Error("Network or source I/O error: ${e.localizedMessage ?: "Unknown I/O error"}", true))
        } catch (e: Exception) {
            emit(UploadEngineResult.Error("Upload failed: ${e.localizedMessage ?: "Unknown error"}", false))
        }
    }

    private fun emitProgress(
        uploadedBytes: Long,
        totalBytes: Long,
        speedCalculator: SpeedCalculator
    ): UploadEngineResult.Progress {
        val speedInfo = speedCalculator.update(uploadedBytes)
        val percentage = ((uploadedBytes * 100f) / totalBytes).coerceIn(0f, 100f)
        val eta = if (speedInfo.currentSpeed > 0) {
            (totalBytes - uploadedBytes) / speedInfo.currentSpeed
        } else 0L
        return UploadEngineResult.Progress(
            UploadProgress(
                uploadedBytes = uploadedBytes,
                totalBytes = totalBytes,
                percentage = percentage,
                speedBytesPerSecond = speedInfo.currentSpeed,
                averageSpeedBytesPerSecond = speedInfo.averageSpeed,
                etaSeconds = eta
            )
        )
    }
}
