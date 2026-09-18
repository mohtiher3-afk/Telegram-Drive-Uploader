package com.telegramdrive.uploader.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class UploadTelemetryFormatterTest {
    @Test
    fun zeroSpeedIsUnknownInsteadOfFabricated() {
        assertEquals("—", formatTransferSpeed(0L))
        assertEquals("—", formatTransferSpeed(-1L))
    }

    @Test
    fun speedUsesBytesPerSecondSuffix() {
        assertEquals("1.00 MB/s", formatTransferSpeed(1_048_576L))
    }

    @Test
    fun etaFormatsMinutesAndSeconds() {
        assertEquals("2m 05s", formatRemainingTime(125L))
        assertEquals("9s", formatRemainingTime(9L))
    }

    @Test
    fun missingEtaIsUnknownUntilARealEstimateExists() {
        assertEquals("", formatRemainingTime(0L))
        assertEquals("", formatRemainingTime(-4L))
    }
}
