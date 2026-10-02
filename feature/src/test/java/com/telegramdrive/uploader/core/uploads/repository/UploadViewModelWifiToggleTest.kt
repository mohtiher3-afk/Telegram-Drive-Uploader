package com.telegramdrive.uploader.core.uploads.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadViewModelWifiToggleTest {

    @Test
    fun wifiOnlyToggleBlocksUploadOnCellularNetwork() {
        assertFalse(shouldAllowUpload(useWifiOnly = true, isWifiConnected = false))
    }

    @Test
    fun wifiOnlyToggleAllowsUploadOnWifiNetwork() {
        assertTrue(shouldAllowUpload(useWifiOnly = true, isWifiConnected = true))
    }

    @Test
    fun disabledToggleAllowsUploadOnAnyNetwork() {
        assertTrue(shouldAllowUpload(useWifiOnly = false, isWifiConnected = true))
        assertTrue(shouldAllowUpload(useWifiOnly = false, isWifiConnected = false))
    }
}