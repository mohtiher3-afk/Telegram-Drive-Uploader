package com.telegramdrive.uploader.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelegramDestinationPolicyTest {
    @Test
    fun sendableChannelIsSelectable() {
        assertTrue(TelegramDestinationPolicy.isSelectable(destination()))
    }

    @Test
    fun destinationWithoutSendPermissionIsRejected() {
        assertFalse(TelegramDestinationPolicy.isSelectable(destination(canSendMessages = false)))
    }

    @Test
    fun unsupportedDestinationTypeIsRejected() {
        assertFalse(
            TelegramDestinationPolicy.isSelectable(
                destination(type = TelegramDestinationType.OTHER)
            )
        )
    }

    @Test
    fun zeroIdIsRejected() {
        assertFalse(TelegramDestinationPolicy.isSelectable(destination(id = 0L)))
    }

    private fun destination(
        id: Long = 100L,
        type: TelegramDestinationType = TelegramDestinationType.CHANNEL,
        canSendMessages: Boolean = true
    ) = TelegramDestination(
        id = id,
        title = "Test channel",
        username = "test_channel",
        type = type,
        photo = null,
        canSendMessages = canSendMessages
    )
}
