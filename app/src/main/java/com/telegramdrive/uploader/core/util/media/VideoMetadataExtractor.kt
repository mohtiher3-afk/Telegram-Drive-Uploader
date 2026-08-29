package com.telegramdrive.uploader.core.util.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object VideoMetadataExtractor {

    suspend fun extractMetadata(context: Context, uriString: Uri): UploadTask = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val resolver = context.contentResolver

        // 1. Persist URI permission if content URI
        if (uriString.scheme == "content") {
            try {
                resolver.takePersistableUriPermission(
                    uriString,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Ignore or log permission failure
            }
        }

        // 2. Query Name and Size
        var fileName = "video_${System.currentTimeMillis()}.mp4"
        var fileSize = 0L
        try {
            resolver.query(uriString, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (_: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_PREPARING,
                severity = DiagnosticSeverity.WARN,
                message = "Video provider metadata is unavailable; continuing with safe defaults.",
                uploadId = id,
                errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE
            )
        }

        // 3. Resolve MIME type from the provider first, then fall back to the real filename extension.
        val mimeType = VideoFormatSupport.normalizeMimeType(resolver.getType(uriString), fileName)
        require(VideoFormatSupport.isSupportedVideo(mimeType, fileName)) {
            "Unsupported video format. Select a video container such as MP4, MKV, MOV, WEBM, AVI, 3GP, TS, MPEG, FLV, WMV, or OGV."
        }

        // 4. Extract Media Metadata (Duration, Width, Height)
        var duration = 0L
        var width = 0
        var height = 0
        val thumbnailPath: String? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uriString)
            
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_PREPARING,
                severity = DiagnosticSeverity.WARN,
                message = "Video media metadata is unavailable; continuing with safe defaults.",
                uploadId = id,
                errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE
            )
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // ignore
            }
        }

        UploadTask(
            id = id,
            sourceUri = uriString.toString(),
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType,
            destinationId = 0L,
            status = UploadStatus.QUEUED,
            progress = 0f,
            uploadedBytes = 0L,
            totalBytes = fileSize,
            speed = 0L,
            averageSpeed = 0L,
            eta = 0L,
            createdAt = System.currentTimeMillis(),
            startedAt = null,
            completedAt = null,
            lastError = null,
            retryCount = 0,
            thumbnailPath = thumbnailPath,
            duration = duration,
            width = width,
            height = height
        )
    }
}
