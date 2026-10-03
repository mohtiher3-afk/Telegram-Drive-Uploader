package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Documents the colour pairings `HomeScreen.kt` is allowed to use, and pins the
 * roles that back them.
 *
 * Why this is a contract test and not a rendered-UI test:
 * `HomeScreen` is a `@Composable` that calls `hiltViewModel()`, so it cannot be
 * composed in a plain unit test without a Hilt container and a fake `HomeUiState`.
 * What *can* be pinned is the scheme half of each pairing. The screen half lives
 * in HomeScreen.kt and is verified on a device, not here — this class does not
 * claim to prove the screen renders, only that the roles it paints with stay
 * readable against one another.
 *
 * The defect this exists to prevent: HomeScreen painted `onPrimary` on a
 * `primaryContainer` canvas. That is not a sanctioned Material pair. Under the
 * dark scheme it measured 1.69:1, so the title, the New Upload label, the
 * Select files headline and the stat icons were unreadable, while every build
 * stayed green.
 */
class HomeScreenColorPairTest {

    /** WCAG 2.1 AA for normal-size text. */
    private val minimumRatio = 4.5f

    /** WCAG 1.4.11 for a non-text boundary such as a card outline. */
    private val minimumBoundaryRatio = 3f

    @Test
    fun `onPrimary on a primaryContainer canvas stays illegible`() {
        // The regression anchor. If a future palette change ever makes this pair
        // legible, this test fails and forces the reason for the HomeScreen
        // rework to be reconsidered rather than silently forgotten.
        val ratio = contrastRatio(AppColors.onPrimary, AppColors.primaryContainer)
        assertTrue(
            "onPrimary on primaryContainer was $ratio:1; HomeScreen must not pair these",
            ratio < minimumRatio
        )
    }

    @Test
    fun `onPrimaryContainer reads on the primaryContainer canvas`() {
        assertPair(AppColors.onPrimaryContainer, AppColors.primaryContainer, minimumRatio)
    }

    @Test
    fun `onSurface reads on the surface the stat cards use`() {
        assertPair(AppColors.onSurface, AppColors.surface, minimumRatio)
    }

    @Test
    fun `the upload card outline clears the 3 to 1 non-text boundary`() {
        // The card fill is the same colour as the canvas behind it, so the outline
        // is the only thing that gives the card an edge. outline measured 1.42:1
        // and outlineVariant 1.23:1 against that canvas; the chosen role does not.
        val ratio = contrastRatio(AppColors.onPrimaryContainer, AppColors.primaryContainer)
        assertTrue(
            "card outline was $ratio:1, below the $minimumBoundaryRatio:1 boundary minimum",
            ratio >= minimumBoundaryRatio
        )
    }

    private fun assertPair(foreground: Color, background: Color, minimum: Float) {
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "pair measured $ratio:1, below $minimum:1",
            ratio >= minimum
        )
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}