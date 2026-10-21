package com.telegramdrive.uploader.feature.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.telegramdrive.uploader.core.ui.theme.*
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract test for the colour pairs `HomeScreen.kt` is allowed to paint with.
 * Validates both Coral Light and Dark schemes.
 */
class HomeScreenColorPairTest {

    private val minimumRatio = 4.5f

    @Test
    fun `dark scheme pairs are compliant`() {
        checkScheme(
            primary = CoralPrimaryDark,
            onPrimary = CoralOnPrimaryDark,
            primaryContainer = CoralPrimaryContainerDark,
            onPrimaryContainer = CoralOnPrimaryContainerDark,
            surface = CoralSurfaceDark,
            onSurface = CoralOnSurfaceDark,
            schemeName = "Dark"
        )
    }

    @Test
    fun `light scheme pairs are compliant`() {
        checkScheme(
            primary = CoralPrimaryLight,
            onPrimary = CoralOnPrimaryLight,
            primaryContainer = CoralPrimaryContainerLight,
            onPrimaryContainer = CoralOnPrimaryContainerLight,
            surface = CoralSurfaceLight,
            onSurface = CoralOnSurfaceLight,
            schemeName = "Light"
        )
    }

    private fun checkScheme(
            primary: Color,
            onPrimary: Color,
            primaryContainer: Color,
            onPrimaryContainer: Color,
            surface: Color,
            onSurface: Color,
            schemeName: String
        ) {
            // All text-on-container pairs must be readable (>= 4.5:1)
            assertReadable(onPrimaryContainer, primaryContainer, "$schemeName onPrimaryContainer on primaryContainer")
            assertReadable(onPrimary, primary, "$schemeName onPrimary on primary")
            assertReadable(onSurface, primaryContainer, "$schemeName onSurface on primaryContainer")
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
