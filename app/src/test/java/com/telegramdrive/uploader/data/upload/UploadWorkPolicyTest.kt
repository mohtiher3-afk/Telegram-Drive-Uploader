package com.telegramdrive.uploader.data.upload

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
}
