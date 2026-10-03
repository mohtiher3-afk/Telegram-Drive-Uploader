package com.telegramdrive.uploader.core.util.media

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure-logic tests for the sizing helpers used by [VideoCompressor]. The
 * MediaCodec pipeline itself is device-only code and cannot be exercised on
 * the JVM, but the buffer/dimension math can be pinned down here.
 */
class VideoCompressorMathTest {
    @Test
    fun copyBufferSizedFromLargestTrackMaxInputSize() {
        assertEquals(
            4_194_304,
            VideoCompressor.computeCopyBufferSizeBytes(listOf(262_144, 4_194_304, 1_048_576))
        )
    }

    @Test
    fun copyBufferNeverSmallerThanHistoricalDefault() {
        assertEquals(
            1_048_576,
            VideoCompressor.computeCopyBufferSizeBytes(emptyList())
        )
        assertEquals(
            1_048_576,
            VideoCompressor.computeCopyBufferSizeBytes(listOf(65_536, 262_144))
        )
    }

    @Test
    fun scaledDimensionEnforcesMinimum() {
        assertEquals(160, VideoCompressor.scaledDimension(0))
        assertEquals(160, VideoCompressor.scaledDimension(100))
    }

    @Test
    fun scaledDimensionAlwaysEven() {
        // 1921 * 1.0 -> odd value must be rounded down to an even one, otherwise
        // H.264 encoder configure() fails.
        assertEquals(1920, VideoCompressor.scaledDimension(1921))
        assertEquals(160, VideoCompressor.scaledDimension(161))
        assertEquals(1080, VideoCompressor.scaledDimension(1080))
    }
}
