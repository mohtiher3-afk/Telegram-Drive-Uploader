package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the token surface every screen compiles against.
 *
 * Screens read `DesignTokens.AppColors.*` and `DesignTokens.AppSpacing.*`. When the
 * grouped objects existed but the `DesignTokens` container did not, the whole
 * `:feature` module failed to compile with "Unresolved reference 'DesignTokens'" and
 * no screen could be built at all. These assertions keep the container and the flat
 * aliases present, and assert that the flat palette stays bound to the light scheme
 * in Theme.kt so the two can never drift apart again.
 */
class DesignTokensTest {

    @Test
    fun `grouped token objects are reachable through the container`() {
        assertSame(AppColors, DesignTokens.AppColors)
        assertSame(AppSpacing, DesignTokens.AppSpacing)
        assertSame(AppContentWidth, DesignTokens.AppContentWidth)
    }

    @Test
    fun `flat aliases keep their grouped values`() {
        assertEquals(AppColors.primaryContainer, DesignTokens.primaryContainer)
        assertEquals(AppSpacing.medium, DesignTokens.spacingM)
    }

    @Test
    fun `flat palette never drifts from the theme scheme`() {
        // AppColors delegates every Material role to the light scheme, so the two
        // cannot diverge. This guards the defect where AppColors restated its own
        // hex values and quietly disagreed with the theme.
        assertEquals(LightColorScheme.primaryContainer, AppColors.primaryContainer)
        assertEquals(LightColorScheme.secondaryContainer, AppColors.secondaryContainer)
        assertEquals(LightColorScheme.surface, AppColors.surface)
        assertEquals(LightColorScheme.onPrimary, AppColors.onPrimary)
        assertEquals(LightColorScheme.onSecondary, AppColors.onSecondary)
        assertEquals(LightColorScheme.onSurface, AppColors.onSurface)
        assertEquals(LightColorScheme.onPrimaryContainer, AppColors.onPrimaryContainer)
        assertEquals(LightColorScheme.onSecondaryContainer, AppColors.onSecondaryContainer)
        assertEquals(LightColorScheme.onSurfaceVariant, AppColors.onSurfaceVariant)
        assertEquals(LightColorScheme.error, AppColors.error)
        assertEquals(LightColorScheme.errorContainer, AppColors.errorContainer)
        assertEquals(LightColorScheme.surfaceContainerLow, AppColors.surfaceContainerLow)
        assertEquals(LightColorScheme.surfaceContainerHigh, AppColors.surfaceContainerHigh)
        assertEquals(LightColorScheme.surfaceContainerHighest, AppColors.surfaceContainerHighest)
    }

    @Test
    fun `surface container ladder stays tonally ordered`() {
        // Low < base < High < Highest is the ladder the card call sites rely on, so
        // assert the relationship instead of pinning duplicate literals here.
        val low = AppColors.surfaceContainerLow.luminance()
        val base = LightColorScheme.surfaceContainer.luminance()
        val high = AppColors.surfaceContainerHigh.luminance()
        val highest = AppColors.surfaceContainerHighest.luminance()
        assertTrue(
            "low > base > high > highest",
            low > base && base > high && high > highest
        )
    }

    @Test
    fun `spacing aliases stay in step with the named scale`() {
        assertEquals(4.dp, AppSpacing.xs)
        assertEquals(8.dp, AppSpacing.sm)
        assertEquals(16.dp, AppSpacing.md)
        assertEquals(24.dp, AppSpacing.lg)
        assertEquals(32.dp, AppSpacing.xl)
        assertEquals(AppSpacing.medium, AppSpacing.phoneEdge)
    }
}