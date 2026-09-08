package com.telegramdrive.uploader.data.upload.reader

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface StreamingFileReader {
    fun getFileSize(uri: Uri): Long
    fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray
    fun copyToFile(uri: Uri, destination: File): Long
}

@Singleton
class StreamingFileReaderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StreamingFileReader {

    override fun getFileSize(uri: Uri): Long {
        return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
            it.length
        } ?: throw IllegalArgumentException("Could not open file size for $uri")
    }

    override fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray {
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalStateException("Could not open seekable input file for $uri")
        return android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
            input.channel.position(offset)
            val buffer = ByteArray(size)
            var bytesRead = 0
            while (bytesRead < size) {
                val result = input.read(buffer, bytesRead, size - bytesRead)
                if (result == -1) break
                bytesRead += result
            }
            if (bytesRead == size) buffer else buffer.copyOf(bytesRead)
        }
    }

    override fun copyToFile(uri: Uri, destination: File): Long {
        destination.parentFile?.mkdirs()
        // A null stream means the source is permanently unreadable (e.g. a content:// grant
        // that died with the picking process). Throw FileNotFoundException so the engine
        // classifies it non-retryable and fails fast with a clear message instead of
        // burning all WorkManager retry attempts on a source that will never open.
        val input = context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Source is no longer readable: $uri. Re-select the file.")
        return input.use { source ->
            destination.outputStream().use { target ->
                copy(source, target)
            }
        }
    }

    private fun copy(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(1024 * 1024)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) return total
            output.write(buffer, 0, count)
            total += count
        }
    }
}
