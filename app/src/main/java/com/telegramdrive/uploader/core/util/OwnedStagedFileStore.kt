package com.telegramdrive.uploader.core.util

import android.content.Context
import android.net.Uri
import com.telegramdrive.uploader.domain.model.UploadTask
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central ownership rules for the app's staging and compression scratch files.
 *
 * These files ARE the upload source: [UploadTask.sourceUri] is rewritten to point at
 * them, so they must live in durable app-private storage ([Context.filesDir]) rather
 * than [android.content.Context.cacheDir]. The OS may evict cacheDir at any time
 * (storage pressure, cache clears, apps-cache management). An eviction between pick
 * time and worker execution silently deletes the source file and turns every queued
 * upload into a permanent SOURCE_FILE_UNAVAILABLE failure, because the original
 * content:// grant was already discarded when the URI was rewritten.
 *
 * Because files under filesDir survive until removed, every file this store owns must
 * be deleted when its task reaches a terminal state that has no retry path
 * (COMPLETED / CANCELLED / manual removal). FAILED and PAUSED keep their files so
 * retry and resume can still read them.
 */
@Singleton
class OwnedStagedFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val stagingDir: File
        get() = File(context.filesDir, "staged-uploads")

    val compressedDir: File
        get() = File(context.filesDir, "compressed")

    /**
     * Deletes every scratch file the task owns: the current [UploadTask.sourceUri]
     * (compressed output, or the staged copy when compression never applied) plus any
     * pre-compression staged copies still present under [stagingDir]. Files outside the
     * app-owned directories are never touched.
     */
    fun deleteOwnedFilesFor(task: UploadTask) {
        deleteOwnedFile(task.sourceUri)
        val prefix = "${task.id}-"
        stagingDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.startsWith(prefix)) {
                runCatching { file.delete() }
            }
        }
    }

    fun deleteOwnedFile(sourceUri: String?): Boolean {
        if (sourceUri.isNullOrBlank()) return false
        val uri = try {
            Uri.parse(sourceUri)
        } catch (_: Exception) {
            return false
        }
        if (uri.scheme != "file") return false
        val path = uri.path ?: return false
        val file = File(path).absoluteFile
        val ownedParent = when {
            file.absolutePath.startsWith(stagingDir.absolutePath + File.separator) -> stagingDir
            file.absolutePath.startsWith(compressedDir.absolutePath + File.separator) -> compressedDir
            else -> return false
        }
        return file.isFile && runCatching { file.delete() }.getOrDefault(false)
    }
}