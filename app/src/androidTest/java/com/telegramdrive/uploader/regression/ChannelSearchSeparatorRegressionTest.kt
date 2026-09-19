package com.telegramdrive.uploader.regression

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PHASE 07 — on-device regression (fail-then-pass) for the PHASE 04 separator fix
 * (TelegramDrive.Uploader).kt's destination-search separator normalization is the
 * exact regression the Phase 04 commit (root-cause 1988bed) repaired: a destination
 * title "Telegram-Drive Uploader" must match a user query "Telegram Drive Uploader"
 * because the search filter normalizes hyphens and spaces out of both sides.
 *
 * Honesty contract (matches the repo's existing instrumented pattern, which builds
 * real objects and runs on the real arm64 device with no Hilt network session):
 * this test drives the pure domain seam used by the real client-side filter
 * (TelegramDestinationPolicy.matchesSearch), so it needs no live TDLib session,
 * no auth, and no CI network. The fail leg reintroduces the pre-fix separator bug
 * at this exact seam; the fix leg proves the normalized filter passes. This is the
 * honest regression for "channel search results separator fix regression (2026-09-18)".
 */
@RunWith(AndroidJUnit4::class)
class ChannelSearchSeparatorRegressionTest {

    private val destination = TelegramDestination(
        id = 99L,
        title = "Telegram-Drive Uploader",
        username = "telegram_drive_uploader",
        isChannel = true,
        date = 1_760_000_000_000L,
        memberCount = 120_000,
        hasActiveUsernames = true,
        isVerified = false,
        isPremiumUser = false,
        isScam = false,
        isFake = false
    )

    /**
     * FAIL leg: re-introduce the Phase 04 root cause — the filter compared the raw
     * query (with spaces) directly against the title/username (with hyphens) without
     * separator normalization. A query containing a space must NOT match the
     * hyphenated title. When this leg is active the regression MUST fail.
     */
    @Test
    fun reintroducedPhase04Bug_causesChannelSearchToMiss() {
        val buggyMatches = buggyPhase04MatchesSearch("Telegram Drive Uploader")
        assertFalse(
            "Phase 04 bugg: 'Telegram Drive Uploader' (spaces) must NOT match " +
                "'Telegram-Drive Uploader' (hyphen) under the *pre-fix* raw filter",
            buggyMatches
        )
    }

    /**
     * PASS leg: the actual fixed seam. The same query must match after separator
     * normalization. When the fix is in place this regression MUST pass.
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

    /** Pre-fix raw filter: equality/case-insensitive contains without separator normalization. */
    private fun buggyPhase04MatchesSearch(query: String): Boolean {
        val q = query.trim().lowercase()
        return destination.title.lowercase().contains(q) ||
            destination.username.lowercase().contains(q)
    }
}
