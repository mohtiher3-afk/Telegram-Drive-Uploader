package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the dark-only palette decision (2026-10-02).
 *
 * The app is dark-only by product decision: the home experience is authored against
 * the near-black reference. `AppColors` therefore delegates to `DarkColorScheme`.
 *
 * Pinning it to `LightColorScheme` made `onPrimary` resolve to white while the
 * screen canvas is `primaryContainer`, a pale lavender. That pairing measured
 * 1.30:1 on a physical device, so the title, the section label and the action
 * button were invisible. A build stayed green through all of it, which is why
 * these pairs are asserted rather than left to review.
 *
 * Known open defect, deliberately NOT asserted here:
 * `HomeScreen.kt` paints text with `onPrimary` on a `primaryContainer` card. That
 * is not a Material-sanctioned pair, and under the dark scheme it measures 1.69:1
 * — still unreadable. Fixing it requires editing HomeScreen.kt, which this change
 * does not touch. Asserting it would fail the build on a defect owned elsewhere.
 */
class AppColorsContrastTest {

    /** WCAG 2.1 AA for normal-size text. */
    private val minimumRatio = 4.5f

    @Test
    fun `onPrimary reads on primary`() {
        assertPair("onPrimary", "primary")
    }

    @Test
    fun `onSurface reads on surface`() {
        assertPair("onSurface", "surface")
    }

    @Test
    fun `onPrimaryContainer reads on primaryContainer`() {
        assertPair("onPrimaryContainer", "primaryContainer")
    }

    @Test
    fun `onSecondaryContainer reads on secondaryContainer`() {
        assertPair("onSecondaryContainer", "secondaryContainer")
    }

    @Test
    fun `onError reads on error`() {
        assertPair("onError", "error")
    }

    @Test
    fun `AppColors delegates to the dark scheme and not the light one`() {
        // Guards the decision itself. A regression to LightColorScheme would still
        // satisfy every pair above, because the light scheme's sanctioned pairs are
        // also legible. Only this assertion distinguishes the two.
        assertEquals(DarkColorScheme.primary, AppColors.primary)
        assertEquals(DarkColorScheme.primaryContainer, AppColors.primaryContainer)
        assertEquals(DarkColorScheme.onPrimary, AppColors.onPrimary)
        assertEquals(DarkColorScheme.onSurface, AppColors.onSurface)
        assertEquals(DarkColorScheme.surface, AppColors.surface)
        assertEquals(DarkColorScheme.error, AppColors.error)
    }

    @Test
    fun `AppColors canvas is dark not pale`() {
        // The regression this change exists to prevent: a pale primaryContainer
        // canvas is what made white text vanish.
        assertTrue(
            "AppColors.primaryContainer must be a dark canvas, was ${AppColors.primaryContainer}",
            AppColors.primaryContainer.luminance() < 0.20f
        )
    }

    private fun assertPair(foregroundRole: String, backgroundRole: String) {
        val foreground = role(AppColors, foregroundRole)
        val background = role(AppColors, backgroundRole)
        val ratio = contrastRatio(foreground, background)
        assertTrue(
            "$foregroundRole on $backgroundRole was $ratio:1, below $minimumRatio:1",
            ratio >= minimumRatio
        )
    }

    private fun role(source: Any, name: String): Color = when (name) {
        "onPrimary" -> AppColors.onPrimary
        "primary" -> AppColors.primary
        "onSurface" -> AppColors.onSurface
        "surface" -> AppColors.surface
        "onPrimaryContainer" -> AppColors.onPrimaryContainer
        "primaryContainer" -> AppColors.primaryContainer
        "onSecondaryContainer" -> AppColors.onSecondaryContainer
        "secondaryContainer" -> AppColors.secondaryContainer
        "onError" -> DarkColorScheme.onError
        "error" -> AppColors.error
        else -> throw IllegalArgumentException("Unknown role: $name")
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
