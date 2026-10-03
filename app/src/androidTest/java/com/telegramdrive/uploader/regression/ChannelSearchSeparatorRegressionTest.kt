package com.telegramdrive.uploader.regression

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationPolicy
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PHASE 07 — on-device regression (fail-then-pass) for the PHASE 04 separator fix.
 *
 * The destination-search separator normalization is the exact regression the Phase 04
 * commit repaired: a destination title "Telegram-Drive Uploader" must match a user query
 * "Telegram Drive Uploader" because the search filter normalizes hyphens and spaces out
 * of both sides.
 *
 * Honesty contract (matches the repository's existing instrumented pattern, which builds
 * real objects and runs on the real device with no Hilt network session): this test drives
 * the pure domain seam used by the real client-side filter
 * ([TelegramDestinationPolicy.matchesSearch]), so it needs no live TDLib session, no auth,
 * and no CI network. The fail leg reintroduces the pre-fix separator bug at this exact
 * seam; the fix leg proves the normalized filter passes.
 */
@RunWith(AndroidJUnit4::class)
class ChannelSearchSeparatorRegressionTest {

    private val destination = TelegramDestination(
        id = 99L,
        title = "Telegram-Drive Uploader",
        username = "telegram_drive_uploader",
        type = TelegramDestinationType.CHANNEL,
        photo = null,
        canSendMessages = true
    )

    /**
     * FAIL leg: re-introduce the Phase 04 root cause — the filter compared the raw query
     * (with spaces) directly against the title/username (with hyphens) without separator
     * normalization, so a space-written query missed the hyphenated title.
     */
    @Test
    fun reintroducedPhase04Bug_causesChannelSearchToMiss() {
        val buggyMatches = buggyPhase04MatchesSearch("Telegram Drive Uploader")
        assertFalse(
            "Phase 04 bug: 'Telegram Drive Uploader' (spaces) must NOT match " +
                "'Telegram-Drive Uploader' (hyphen) under the pre-fix raw filter",
            buggyMatches
        )
    }

    /**
     * PASS leg: the actual fixed seam. The same query must match after separator
     * normalization.
     */
    @Test
    fun fixedSeam_channelSearchMatchesAcrossSeparators() {
        val fixedMatches = TelegramDestinationPolicy.matchesSearch(
            destination = destination,
            query = "Telegram Drive Uploader"
        )
        assertTrue(
            "Phase 04 fix: 'Telegram Drive Uploader' (spaces) must match " +
                "'Telegram-Drive Uploader' (hyphen) under the normalized filter",
            fixedMatches
        )
    }

    @Test
    fun fixedSeam_unrelatedQueryStillExcluded() {
        val unrelated = TelegramDestinationPolicy.matchesSearch(
            destination = destination,
            query = "Netflix"
        )
        assertFalse(
            "Phase 04 fix must not over-match: 'Netflix' must not match the channel",
            unrelated
        )
    }

    /** Pre-fix raw filter: case-insensitive contains without separator normalization. */
    private fun buggyPhase04MatchesSearch(query: String): Boolean {
        val q = query.trim().lowercase()
        val username = destination.username?.lowercase()
        return destination.title.lowercase().contains(q) || username?.contains(q) == true
    }
}
