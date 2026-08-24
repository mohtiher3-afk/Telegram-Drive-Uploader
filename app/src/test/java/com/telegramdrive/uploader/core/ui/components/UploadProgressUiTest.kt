package com.telegramdrive.uploader.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class UploadProgressUiTest {
    @Test
    fun percentageIsConvertedToProgressFraction() {
        assertEquals(0f, uploadProgressFraction(0f), 0.0001f)
        assertEquals(0.5f, uploadProgressFraction(50f), 0.0001f)
        assertEquals(1f, uploadProgressFraction(100f), 0.0001f)
    }

    @Test
    fun percentageFractionIsClampedToDisplayBounds() {
        assertEquals(0f, uploadProgressFraction(-10f), 0.0001f)
        assertEquals(1f, uploadProgressFraction(125f), 0.0001f)
    }

    @Test
    fun percentageLabelUsesThePersistedPercentageScale() {
        assertEquals(0, uploadProgressPercent(0f))
        assertEquals(50, uploadProgressPercent(50.9f))
        assertEquals(100, uploadProgressPercent(125f))
    }
}
