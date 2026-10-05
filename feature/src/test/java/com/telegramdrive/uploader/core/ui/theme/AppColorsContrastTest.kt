package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG 2.1 contrast gate for the Mission Control palette.
 *
 * Every pair below is a combination the UI actually renders, so this test is the
 * one place that catches a token change that would make text unreadable. The
 * threshold is 4.5:1, the WCAG AA requirement for normal-size body text.
 *
 * The palette is Mission Control, dark surface only: these roles are declared on
 * [AppColors] and are not derived from a Material scheme, so nothing re-derives
 * them per-theme and this file cannot silently drift.
 *
 * Two surfaces are gated separately on purpose. [AppColors.surfaceCard] is one step
 * above [AppColors.background], so text drawn on a card has to clear 4.5:1 against the
 * card colour and not merely against the canvas. A role that passes on the canvas can
 * still fail on the card, which is exactly how a contrast regression hides.
 */
class AppColorsContrastTest {

    @Test
    fun `body text on the mission background is readable`() {
        assertPair(AppColors.contentPrimary, AppColors.background, "contentPrimary/background")
        assertPair(AppColors.contentMuted, AppColors.background, "contentMuted/background")
    }

    @Test
    fun `text on solid accent fills is readable`() {
        assertPair(AppColors.onLime, AppColors.lime, "onLime/lime")
        assertPair(AppColors.onPurple, AppColors.purple, "onPurple/purple")
    }

    @Test
    fun `accent and status colors stand out against the background`() {
        assertPair(AppColors.teal, AppColors.background, "teal/background")
        assertPair(AppColors.danger, AppColors.background, "danger/background")
        assertPair(AppColors.amber, AppColors.background, "amber/background")
        assertPair(AppColors.purpleHot, AppColors.background, "purpleHot/background")
    }

    @Test
    fun `body text on a raised card is readable`() {
        assertPair(AppColors.contentPrimary, AppColors.surfaceCard, "contentPrimary/surfaceCard")
        assertPair(AppColors.contentMuted, AppColors.surfaceCard, "contentMuted/surfaceCard")
    }

    /**
     * The action lime is the app's only action colour (buttons, progress fills, active
     * tab), and progress fills are drawn on cards as well as on the canvas, so it has
     * to clear AA on both surfaces.
     */
    @Test
    fun `action lime is legible on both surfaces`() {
        assertPair(AppColors.lime, AppColors.background, "lime/background")
        assertPair(AppColors.limeLight, AppColors.background, "limeLight/background")
        assertPair(AppColors.lime, AppColors.surfaceCard, "lime/surfaceCard")
    }

    /**
     * The brand violet sits inside the large-text and non-text band on purpose.
     *
     * It is a glow, a border, and a heading accent, so it is held to WCAG's 3:1 rather
     * than the 4.5:1 body-text rule. This test fails in both directions on purpose: if
     * a palette edit pushes the violet below 3:1 it can no longer mark a boundary, and
     * if it climbs above 4.5:1 that signals it has become a text colour and the role
     * documentation is now wrong. Either outcome deserves a deliberate decision rather
     * than a silent drift.
     */
    @Test
    fun `brand violet stays in the large-text and boundary band`() {
        val ratio = contrastRatio(AppColors.purple, AppColors.background)
        assertTrue(
            "purple/background measured $ratio:1, expected at least $NON_TEXT_RATIO:1 " +
                "so it can still mark a boundary or a large heading",
            ratio >= NON_TEXT_RATIO
        )
        assertTrue(
            "purple/background measured $ratio:1 and now clears the body-text threshold; " +
                "it is documented as an accent, so its role needs re-evaluating",
            ratio < MIN_RATIO
        )
    }

    private fun assertPair(foreground: Color, background: Color, name: String) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "$name contrast was ${"%.2f".format(ratio)}:1, expected at least $MIN_RATIO:1",
            ratio >= MIN_RATIO
        )
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    private companion object {
        /** WCAG AA for normal text. */
        const val MIN_RATIO = 4.5f

        /** WCAG 1.4.11 for large text and non-text boundaries. */
        const val NON_TEXT_RATIO = 3f
    }
}