package com.telegramdrive.uploader.domain.upload

import com.telegramdrive.uploader.domain.model.UploadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UploadEventNotificationPolicyTest {

    @Test
    fun `completed status emits only a completed notification event`() {
        assertEquals(
            UploadEventNotificationEvent.COMPLETED,
            UploadEventNotificationPolicy.eventFor(UploadStatus.COMPLETED)
        )
    }

    @Test
    fun `failed status emits only a failed notification event`() {
        assertEquals(
            UploadEventNotificationEvent.FAILED,
            UploadEventNotificationPolicy.eventFor(UploadStatus.FAILED)
        )
    }

    @Test
    fun `non terminal states never emit notifications`() {
        listOf(
            UploadStatus.QUEUED,
            UploadStatus.PREPARING,
            UploadStatus.UPLOADING,
            UploadStatus.PAUSED,
            UploadStatus.RETRYING,
            UploadStatus.CANCELLED
        ).forEach { status ->
            assertNull(UploadEventNotificationPolicy.eventFor(status))
        }
    }
}
