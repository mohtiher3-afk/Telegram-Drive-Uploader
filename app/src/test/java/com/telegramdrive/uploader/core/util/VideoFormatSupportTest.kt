package com.telegramdrive.uploader.core.util

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
    fun rejectsNonVideoFiles() {
        assertFalse(VideoFormatSupport.isSupportedVideo("application/pdf", "document.pdf"))
        assertFalse(VideoFormatSupport.isSupportedVideo("image/jpeg", "photo.jpg"))
    }
}
