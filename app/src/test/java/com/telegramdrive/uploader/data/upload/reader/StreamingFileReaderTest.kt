package com.telegramdrive.uploader.data.upload.reader

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import java.io.ByteArrayInputStream
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class StreamingFileReaderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val reader = StreamingFileReaderImpl(context)

    @Test
    fun `copyToFile writes the source bytes to the destination`() {
        val uri = Uri.parse("content://com.example/provider/clip.mp4")
        val payload = ByteArray(4096) { (it % 251).toByte() }
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(payload))

        val destination = File.createTempFile("staged-", ".mp4")
        try {
            val copied = reader.copyToFile(uri, destination)
            assertTrue("Expected all bytes copied, got $copied", copied == payload.size.toLong())
            assertArrayEquals(payload, destination.readBytes())
        } finally {
            destination.delete()
        }
    }
}
