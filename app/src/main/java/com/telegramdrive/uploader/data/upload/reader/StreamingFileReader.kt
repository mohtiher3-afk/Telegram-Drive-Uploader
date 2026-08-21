package com.telegramdrive.uploader.data.upload.reader

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
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
        val descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            ?: throw IllegalStateException("Could not open seekable input file for $uri")
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
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
}
