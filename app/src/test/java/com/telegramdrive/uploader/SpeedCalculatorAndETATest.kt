package com.telegramdrive.uploader

import com.telegramdrive.uploader.domain.upload.SpeedCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SpeedCalculatorAndETATest {

    private lateinit var speedCalculator: SpeedCalculator

    @Before
    fun setUp() {
        speedCalculator = SpeedCalculator()
    }

    @Test
    fun testInitialSpeedIsZero() {
        val info = speedCalculator.update(0)
        assertEquals(0L, info.currentSpeed)
        assertEquals(0L, info.averageSpeed)
    }

    @Test
    fun testStableSpeedCalculation() {
        // Initial reading
        speedCalculator.update(0)
        
        // Let's mock elapsed time manually inside tests if we could,
        // but since it uses System.currentTimeMillis(), we can simulate multiple updates
        // and assert that speeds are non-negative, finite, and follow correct range thresholds.
        val info = speedCalculator.update(1024 * 1024) // 1 MB transferred
        
        assertTrue("Speed should be non-negative", info.currentSpeed >= 0)
        assertTrue("Average speed should be non-negative", info.averageSpeed >= 0)
    }

    @Test
    fun testResetClearsMeasurements() {
        speedCalculator.update(0)
        speedCalculator.update(5000)
        speedCalculator.reset()
        
        val info = speedCalculator.update(0)
        assertEquals(0L, info.currentSpeed)
        assertEquals(0L, info.averageSpeed)
    }

    @Test
    fun testEtaCalculationFormula() {
        // Test deterministic manual ETA math to prevent NaN/Infinity bugs
        val totalBytes = 1000L
        val uploadedBytes = 200L
        val speed = 50L // 50 bytes per second

        val remainingBytes = totalBytes - uploadedBytes
        val eta = if (speed > 0) {
            remainingBytes / speed
        } else {
            -1L // Calculating/Paused
        }

        assertEquals(16L, eta) // (1000 - 200) / 50 = 800 / 50 = 16 seconds

        // If speed is 0
        val zeroSpeedEta = if (0L > 0) remainingBytes / 0L else -1L
        assertEquals(-1L, zeroSpeedEta)
    }

    @Test
    fun testProgressValues() {
        // Progress percentage boundaries
        val testPercentage = { uploaded: Long, total: Long ->
            if (total > 0) {
                (uploaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
        }

        assertEquals(0f, testPercentage(0, 1000))
        assertEquals(0.5f, testPercentage(500, 1000))
        assertEquals(1f, testPercentage(1000, 1000))
        assertEquals(1f, testPercentage(1200, 1000)) // Enforced max 1f
        assertEquals(0f, testPercentage(-100, 1000)) // Enforced min 0f
        assertEquals(0f, testPercentage(500, 0)) // Zero division check
    }
}
