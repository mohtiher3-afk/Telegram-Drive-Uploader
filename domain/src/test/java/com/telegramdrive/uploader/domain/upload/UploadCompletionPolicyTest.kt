package com.telegramdrive.uploader.domain.upload

import org.junit.Assert.assertEquals
import org.junit.Test

class UploadCompletionPolicyTest {
    @Test
    fun unconfirmedTelegramDeliveryCannotBeMarkedCompleted() {
        assertEquals(
            UploadCompletionPolicy.Decision.UNCONFIRMED,
            UploadCompletionPolicy.decide(hasConfirmedTelegramDelivery = false)
        )
    }

    @Test
    fun onlyConfirmedTelegramDeliveryCanBeMarkedCompleted() {
        assertEquals(
            UploadCompletionPolicy.Decision.CONFIRMED,
            UploadCompletionPolicy.decide(hasConfirmedTelegramDelivery = true)
        )
    }
}

