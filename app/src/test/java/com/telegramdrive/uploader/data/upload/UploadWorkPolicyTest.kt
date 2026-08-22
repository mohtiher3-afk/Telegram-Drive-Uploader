package com.telegramdrive.uploader.data.upload

import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UploadWorkPolicyTest {
    @Test
    fun uploadRequiresNetworkButNotBatteryThreshold() {
        val constraints = UploadWorkPolicy.constraints()

        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
        assertFalse(constraints.requiresBatteryNotLow())
    }

    @Test
    fun initialWorkKeepsActiveRequest() {
        assertEquals(ExistingWorkPolicy.KEEP, UploadWorkPolicy.existingWorkPolicy(false))
    }

    @Test
    fun retryAndResumeReplaceTerminalRequest() {
        assertEquals(ExistingWorkPolicy.REPLACE, UploadWorkPolicy.existingWorkPolicy(true))
    }
}
