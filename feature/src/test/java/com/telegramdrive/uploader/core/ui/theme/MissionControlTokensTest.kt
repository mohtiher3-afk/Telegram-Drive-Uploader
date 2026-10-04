package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the Mission Control surface tokens.
 *
 * The dark-surface roles ([AppColors.background], [AppColors.contentPrimary],
 * [AppColors.lime], [AppColors.teal], [AppColors.purple] and the glass alphas) are
 * separate from the Material-delegating roles because `onSurface` and `error` are
 * bound to the light scheme and are unusable on the dark canvas. Two invariants keep
 * that split honest, and both are cheap to break by accident:
 *
 * 1. The translucency alphas stay on the token. A screen that re-composes its own
 *    alpha silently drifts from the design system.
 * 2. The canvas stays dark. A future "add light mode" edit that repoints these roles
 *    is caught here instead of shipping as an unreadable screen.
 *
 * Readability of each role against the canvas is covered by [AppColorsContrastTest].
 */
class MissionControlTokensTest {

    @Test
    fun `mission surface tokens are reachable through the container`() {
        assertSame(AppColors, DesignTokens.AppColors)
    }

    @Test
    fun `glass alphas stay attached to their tokens`() {
        // Tolerance is 1/255, not 0: Compose packs Color alpha into 8 bits, so a
        // declared 0.03f reads back as 0.03137. Asserting closer than one 8-bit step
        // would fail on the format, not on a design change.
        val quantum = 1f / 255f
        assertEquals(0.03f, AppColors.gridLine.alpha, quantum)
        assertEquals(0.055f, AppColors.glassFill.alpha, quantum)
        assertEquals(0.10f, AppColors.glassBorder.alpha, quantum)
    }

    @Test
    fun `mission canvas is darker than every text role drawn on it`() {
        val canvas = AppColors.background.luminance()
        listOf(AppColors.contentPrimary, AppColors.contentMuted, AppColors.lime)
            .forEach { role ->
                assertTrue(
                    "${role} must be lighter than the canvas",
                    role.luminance() > canvas
                )
            }
    }

    /**
     * Lime is the only action color in the system, so a second near-lime role would
     * mean two different "the primary button" colors. LimeLight is the one permitted
     * sibling and must stay a highlight of Lime rather than an independent accent.
     */
    @Test
    fun `lime light is a highlight of lime, not a second action color`() {
        assertTrue(AppColors.limeLight.luminance() > AppColors.lime.luminance())
        // LimeLight is a tint, not a hue shift: green must still dominate.
        assertTrue(
            "limeLight drifted away from lime's hue",
            AppColors.limeLight.green > AppColors.limeLight.blue
        )
    }
}