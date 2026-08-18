package com.telegramdrive.uploader.data.upload.reader

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

interface StreamingFileReader {
    fun getFileSize(uri: Uri): Long
    fun readChunk(uri: Uri, offset: Long, size: Int): ByteArray
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
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val skipped = inputStream.skip(offset)
            if (skipped < offset) {
                // For some streams, skip() might not skip all bytes. We need to read them manually if necessary.
                var remaining = offset - skipped
                while (remaining > 0) {
                    val skippedNow = inputStream.skip(remaining)
                    if (skippedNow == 0L) {
                        if (inputStream.read() == -1) break
                        remaining--
                    } else {
                        remaining -= skippedNow
                    }
                }
            }
            
            val buffer = ByteArray(size)
            var bytesRead = 0
            while (bytesRead < size) {
                val result = inputStream.read(buffer, bytesRead, size - bytesRead)
                if (result == -1) break
                bytesRead += result
            }
            if (bytesRead == size) buffer else buffer.copyOf(bytesRead)
        } ?: throw IllegalStateException("Could not open input stream for $uri")
    }
}
