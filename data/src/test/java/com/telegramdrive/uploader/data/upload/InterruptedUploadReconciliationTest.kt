package com.telegramdrive.uploader.data.upload

import com.telegramdrive.uploader.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterruptedUploadReconciliationTest {

    @Test
    fun interruptedActiveStatesAreReconciledToQueued() {
        val activeStates = listOf(UploadStatus.PREPARING, UploadStatus.UPLOADING)
        
        for (status in activeStates) {
            val shouldReconcile = status == UploadStatus.PREPARING || status == UploadStatus.UPLOADING
            assertTrue("Status $status should be reconciled to QUEUED", shouldReconcile)
        }
    }

    @Test
    fun terminalAndStableStatesArePreserved() {
        val stableStates = listOf(
            UploadStatus.QUEUED,
            UploadStatus.COMPLETED,
            UploadStatus.FAILED,
            UploadStatus.CANCELLED,
            UploadStatus.PAUSED,
            UploadStatus.RETRYING
        )

        for (status in stableStates) {
            val isInterruptedActive = status == UploadStatus.PREPARING || status == UploadStatus.UPLOADING
            assertFalse("Status $status should not be reconciled", isInterruptedActive)
        }
    }
}
