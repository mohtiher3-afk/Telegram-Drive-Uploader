package com.telegramdrive.uploader.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class TransferMetricsTest {
    @Test
    fun testSpeedCalculationBounds() {
        val maxTrackedSpeed = 10_485_760L // 10 MB/s
        
        // Zero speed
        val ratioZero = (0L.toFloat() / maxTrackedSpeed).coerceIn(0f, 1f)
        assertEquals(0f, ratioZero)
        
        // Mid speed
        val ratioMid = (5_242_880L.toFloat() / maxTrackedSpeed).coerceIn(0f, 1f)
        assertEquals(0.5f, ratioMid, 0.001f)
        
        // Excess speed capped at 1.0
        val ratioExcess = (20_971_520L.toFloat() / maxTrackedSpeed).coerceIn(0f, 1f)
        assertEquals(1f, ratioExcess)
    }

    @Test
    fun testEtaProgressBounds() {
        // Full progress remaining
        val progressZero = 0f
        val remainingRatioFull = (1f - progressZero).coerceIn(0f, 1f)
        assertEquals(1f, remainingRatioFull)

        // Mid progress remaining
        val progressMid = 0.4f
        val remainingRatioMid = (1f - progressMid).coerceIn(0f, 1f)
        assertEquals(0.6f, remainingRatioMid, 0.001f)

        // Complete progress
        val progressDone = 1.0f
        val remainingRatioDone = (1f - progressDone).coerceIn(0f, 1f)
        assertEquals(0f, remainingRatioDone)
    }
}
