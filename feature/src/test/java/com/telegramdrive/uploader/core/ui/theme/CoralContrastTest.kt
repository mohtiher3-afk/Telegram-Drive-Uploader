package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WCAG 2.1 contrast gate for the Coral palette (fallback scheme).
 * Validates both Light and Dark schemes to ensure accessibility baseline.
 */
class CoralContrastTest {

    @Test
    fun `dark scheme contrast is readable`() {
        checkScheme(
            surface = CoralSurfaceDark,
            onSurface = CoralOnSurfaceDark,
            onSurfaceVariant = CoralOnSurfaceVariantDark,
            primary = CoralPrimaryDark,
            onPrimary = CoralOnPrimaryDark,
            primaryContainer = CoralPrimaryContainerDark,
            onPrimaryContainer = CoralOnPrimaryContainerDark,
            error = CoralErrorDark,
            schemeName = "Dark"
        )
    }

    @Test
    fun `light scheme contrast is readable`() {
        checkScheme(
            surface = CoralSurfaceLight,
            onSurface = CoralOnSurfaceLight,
            onSurfaceVariant = CoralOnSurfaceVariantLight,
            primary = CoralPrimaryLight,
            onPrimary = CoralOnPrimaryLight,
            primaryContainer = CoralPrimaryContainerLight,
            onPrimaryContainer = CoralOnPrimaryContainerLight,
            error = CoralErrorLight,
            schemeName = "Light"
        )
    }

    private fun checkScheme(
        surface: Color,
        onSurface: Color,
        onSurfaceVariant: Color,
        primary: Color,
        onPrimary: Color,
        primaryContainer: Color,
        onPrimaryContainer: Color,
        error: Color,
        schemeName: String
    ) {
        assertPair(onSurface, surface, "$schemeName onSurface/surface", 4.5f)
        assertPair(onSurfaceVariant, surface, "$schemeName onSurfaceVariant/surface", 4.5f)
        assertPair(onPrimary, primary, "$schemeName onPrimary/primary", 4.5f)
        assertPair(onPrimaryContainer, primaryContainer, "$schemeName onPrimaryContainer/primaryContainer", 4.5f)
        assertPair(error, surface, "$schemeName error/surface", 3.0f)
        assertPair(primary, surface, "$schemeName primary/surface", 3.0f)
    }

    private fun assertPair(foreground: Color, background: Color, name: String, threshold: Float) {
        val ratio = contrastRatio(foreground, background)
        println("$name ratio: ${"%.2f".format(ratio)}:1")
        assertTrue(
            "$name contrast was ${"%.2f".format(ratio)}:1, expected at least $threshold:1",
            ratio >= threshold
        )
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}