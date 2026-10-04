package com.telegramdrive.uploader.feature.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.telegramdrive.uploader.core.ui.theme.AppColors
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract test for the colour pairs `HomeScreen.kt` is allowed to paint with.
 *
 * Why this is a token test and not a rendered-UI test:
 * `HomeScreen` is a `@Composable` that calls `hiltViewModel()`, so it cannot be composed
 * in a plain JVM test without a Hilt container and a fake `HomeUiState`. What *can* be
 * pinned here is the scheme half of each pairing. The screen half is verified on a
 * device, not here — this class does not claim to prove the screen renders, only that
 * the roles it paints with stay readable against one another.
 *
 * The defect this exists to prevent:
 * `HomeScreen` painted `onPrimary` on a `primaryContainer` canvas. That is not a
 * Material-sanctioned pair. It measures 1.30:1 on the light scheme, so the greeting,
 * the New Upload label, the Select files headline and the tonal button were effectively
 * invisible — while every build stayed green, which is why these pairs are asserted
 * rather than left to review.
 */
class HomeScreenColorPairTest {

    /** WCAG 2.1 AA for normal-size text. */
    private val minimumRatio = 4.5f

    @Test
    fun `onPrimary on a primaryContainer canvas stays illegible`() {
        // The regression anchor. If a future palette change ever makes this pair
        // legible, this test fails and forces the reason for the HomeScreen rework to be
        // reconsidered rather than silently forgotten.
        val ratio = contrastRatio(AppColors.onPrimary, AppColors.primaryContainer)
        assertTrue(
            "onPrimary on primaryContainer measured $ratio:1 and is no longer the " +
                "documented defect; revisit the HomeScreen colour decision",
            ratio < minimumRatio
        )
    }

    @Test
    fun `text on the primaryContainer canvas uses a readable role`() {
        // This is the pair HomeScreen must use after the fix.
        assertReadable(AppColors.onPrimaryContainer, AppColors.primaryContainer, "onPrimaryContainer on primaryContainer")
    }

    @Test
    fun `text on the tonal button fill stays readable`() {
        // The Select files button currently fills with onPrimary and labels with
        // primaryContainer. That inversion is the same defect as the canvas above: it
        // measures 1.30:1. The sanctioned filled-button pair is onPrimary on primary
        // (6.23:1), so this asserts the pair the button must use after the fix.
        assertReadable(AppColors.onPrimary, AppColors.primary, "onPrimary on primary")
    }

    @Test
    fun `body text on the home canvas uses a readable role`() {
        assertReadable(AppColors.onSurface, AppColors.primaryContainer, "onSurface on primaryContainer")
    }

    @Test
    fun `muted text on the home canvas uses a readable role`() {
        assertReadable(AppColors.onSurfaceVariant, AppColors.primaryContainer, "onSurfaceVariant on primaryContainer")
    }

    private fun assertReadable(foreground: Color, background: Color, name: String) {
        val ratio = contrastRatio(foreground, background)
        assertTrue("$name measured $ratio:1, expected at least $minimumRatio:1", ratio >= minimumRatio)
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}