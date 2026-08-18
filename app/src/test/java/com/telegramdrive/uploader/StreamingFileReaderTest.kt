package com.telegramdrive.uploader

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.data.upload.reader.StreamingFileReaderImpl
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StreamingFileReaderTest {

    private lateinit var context: Context
    private lateinit var fileReader: StreamingFileReaderImpl
    private lateinit var testFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileReader = StreamingFileReaderImpl(context)
        
        // Create a 1 MB deterministic test file with a known repeating byte pattern
        testFile = File(context.cacheDir, "deterministic_1mb.mp4")
        FileOutputStream(testFile).use { out ->
            val pattern = ByteArray(1024) { i -> (i % 256).toByte() }
            for (i in 0 until 1024) { // 1024 * 1024 = 1 MB
                out.write(pattern)
            }
        }
    }

    @Test
    fun testGetFileSize() {
        val uri = Uri.fromFile(testFile)
        val size = fileReader.getFileSize(uri)
        assertEquals(1024 * 1024L, size)
    }

    @Test
    fun testReadFirstPart() {
        val uri = Uri.fromFile(testFile)
        // Read first 512 bytes
        val chunk = fileReader.readChunk(uri, offset = 0, size = 512)
        assertEquals(512, chunk.size)
        // Verify known byte ordering
        for (i in 0 until 512) {
            assertEquals((i % 256).toByte(), chunk[i])
        }
    }

    @Test
    fun testReadMiddlePart() {
        val uri = Uri.fromFile(testFile)
        // Read 512 bytes starting at offset 1024
        val chunk = fileReader.readChunk(uri, offset = 1024, size = 512)
        assertEquals(512, chunk.size)
        for (i in 0 until 512) {
            assertEquals((i % 256).toByte(), chunk[i])
        }
    }

    @Test
    fun testReadFileSmallerThanRequestedSize() {
        val uri = Uri.fromFile(testFile)
        // Read starting very close to the end of the 1 MB file (last 10 bytes) but ask for 100 bytes
        val offset = (1024 * 1024 - 10).toLong()
        val chunk = fileReader.readChunk(uri, offset = offset, size = 100)
        assertEquals(10, chunk.size) // Should only return remaining 10 bytes
    }

    @Test
    fun testReadPartAtExactBoundary() {
        val uri = Uri.fromFile(testFile)
        val offset = (1024 * 1024).toLong()
        val chunk = fileReader.readChunk(uri, offset = offset, size = 100)
        assertEquals(0, chunk.size) // Beyond file end, should be empty
    }

    @Test
    fun testEmptyFileReturnsZeroBytes() {
        val emptyFile = File(context.cacheDir, "empty.mp4")
        emptyFile.createNewFile()
        val uri = Uri.fromFile(emptyFile)

        val size = fileReader.getFileSize(uri)
        assertEquals(0L, size)

        val chunk = fileReader.readChunk(uri, offset = 0, size = 100)
        assertEquals(0, chunk.size)
    }

    @Test(expected = Exception::class)
    fun testMissingFileThrowsException() {
        val missingFile = File(context.cacheDir, "non_existent.mp4")
        val uri = Uri.fromFile(missingFile)
        fileReader.getFileSize(uri)
    }
}
