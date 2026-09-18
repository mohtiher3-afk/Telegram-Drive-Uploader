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

    @Test
    fun hyphenatedTitleMatchesSpaceSeparatedQuery() {
        val channel = destination(title = "Telegram-Drive-Uploader", username = null)
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "Telegram Drive Uploader"))
    }

    @Test
    fun spaceSeparatedTitleMatchesHyphenatedQuery() {
        val channel = destination(title = "Telegram Drive Uploader", username = null)
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "Telegram-Drive-Uploader"))
    }

    @Test
    fun searchIsCaseInsensitive() {
        val channel = destination(title = "Telegram-Drive-Uploader", username = null)
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "telegram drive uploader"))
    }

    @Test
    fun usernameMatchesWithArbitrarySeparators() {
        val channel = destination(title = "Unrelated", username = "telegram-drive uploader")
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "TelegramDriveUploader"))
    }

    @Test
    fun leadingAtIsIgnoredInQuery() {
        val channel = destination(title = "Telegram-Drive-Uploader", username = null)
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "@Telegram Drive Uploader"))
    }

    @Test
    fun unrelatedDestinationDoesNotMatch() {
        val channel = destination(title = "Some other channel", username = null)
        assertFalse(TelegramDestinationPolicy.matchesSearch(channel, "Telegram Drive Uploader"))
    }

    @Test
    fun blankQueryMatchesEverything() {
        val channel = destination(title = "Anything")
        assertTrue(TelegramDestinationPolicy.matchesSearch(channel, "   "))
    }

    private fun destination(
        id: Long = 100L,
        type: TelegramDestinationType = TelegramDestinationType.CHANNEL,
        canSendMessages: Boolean = true,
        title: String = "Test channel",
        username: String? = "test_channel"
    ) = TelegramDestination(
        id = id,
        title = title,
        username = username,
        type = type,
        photo = null,
        canSendMessages = canSendMessages
    )
}