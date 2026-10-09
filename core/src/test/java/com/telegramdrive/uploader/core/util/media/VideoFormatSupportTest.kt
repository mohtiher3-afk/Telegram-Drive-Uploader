package com.telegramdrive.uploader.core.util.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFormatSupportTest {
    @Test
    fun recognizesCommonContainersFromExtensions() {
        listOf("mp4", "mkv", "mov", "webm", "avi", "3gp", "ts", "mpeg", "flv", "wmv", "ogv")
            .forEach { extension ->
                assertTrue("Expected .$extension to be recognized", VideoFormatSupport.isSupportedVideo("", "clip.$extension"))
            }
    }

    @Test
    fun recognizesAdditionalContainersFromExtensions() {
        listOf("m4v", "3g2", "m2ts", "mts", "qt", "mpe", "asf")
            .forEach { extension ->
                assertTrue("Expected .$extension to be recognized", VideoFormatSupport.isSupportedVideo("", "clip.$extension"))
            }
    }

    @Test
    fun providerGenericMimeFallsBackToVideoExtension() {
        assertEquals(
            "video/x-matroska",
            VideoFormatSupport.normalizeMimeType("application/octet-stream", "camera.mkv")
        )
        assertEquals(
            "video/quicktime",
            VideoFormatSupport.normalizeMimeType(null, "camera.mov")
        )
    }

    @Test
    fun normalizesReportedMimeCaseAndWhitespace() {
        assertEquals(
            "video/mp4",
            VideoFormatSupport.normalizeMimeType("  VIDEO/MP4  ", "clip.mp4")
        )
        assertEquals(
            "video/quicktime",
            VideoFormatSupport.normalizeMimeType("Video/QuickTime", "clip.mov")
        )
    }

    @Test
    fun emptyReportedMimeWithUnknownExtensionReturnsEmpty() {
        assertEquals("", VideoFormatSupport.normalizeMimeType(null, "no_extension"))
        assertEquals("", VideoFormatSupport.normalizeMimeType("", "clip.txt"))
    }

    @Test
    fun upperCaseExtensionIsNormalized() {
        assertEquals(
            "video/x-matroska",
            VideoFormatSupport.normalizeMimeType(null, "camera.MKV")
        )
        assertTrue(VideoFormatSupport.isSupportedVideo("", "camera.MOV"))
    }

    @Test
    fun rejectsNonVideoFiles() {
        assertFalse(VideoFormatSupport.isSupportedVideo("application/pdf", "document.pdf"))
        assertFalse(VideoFormatSupport.isSupportedVideo("image/jpeg", "photo.jpg"))
    }

    @Test
    fun rejectsNonVideoEvenWhenExtensionLooksPlausibleButMimeContradicts() {
        // The reported MIME is the stronger signal: a real "application/pdf" must
        // not be accepted just because the file name ends in .mp4.
        assertFalse(VideoFormatSupport.isSupportedVideo("application/pdf", "document.mp4"))
    }
}
