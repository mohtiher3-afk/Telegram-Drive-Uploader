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
    }

    /**
     * Lime is the app's only action color (buttons, progress, active tab), so it
     * must also be legible as a foreground on the dark background.
     */
    @Test
    fun `action lime is legible on the background`() {
        assertPair(AppColors.lime, AppColors.background, "lime/background")
        assertPair(AppColors.limeLight, AppColors.background, "limeLight/background")
    }

    private fun assertPair(foreground: Color, background: Color, name: String) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "$name contrast was ${"%.2f".format(ratio)}:1, expected at least 4.5:1",
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
    }
}