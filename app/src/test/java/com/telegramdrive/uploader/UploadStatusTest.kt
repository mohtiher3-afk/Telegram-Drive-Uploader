package com.telegramdrive.uploader

import com.telegramdrive.uploader.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class UploadStatusTest {
    @Test
    fun testUploadStatusEnum() {
        assertEquals("QUEUED", UploadStatus.QUEUED.name)
        assertEquals("UPLOADING", UploadStatus.UPLOADING.name)
        assertEquals("PAUSED", UploadStatus.PAUSED.name)
        assertEquals("COMPLETED", UploadStatus.COMPLETED.name)
        assertEquals("FAILED", UploadStatus.FAILED.name)
        assertEquals("CANCELLED", UploadStatus.CANCELLED.name)
    }
}
