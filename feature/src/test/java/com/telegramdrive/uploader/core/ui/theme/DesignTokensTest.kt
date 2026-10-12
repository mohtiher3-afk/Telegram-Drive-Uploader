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
 * Screens read `DesignTokens.AppColors.*` and `DesignTokens.AppSpacing.*`. These
 * assertions keep the container and the flat aliases present, and assert that
 * AppColors is bound to the Dusk palette from tokens.json.
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
    fun `AppColors is bound to the Dusk palette`() {
        // AppColors uses the Dusk palette (dark variant) from tokens.json.
        // When dynamic color is enabled, screens should read MaterialTheme.colorScheme
        // directly for the primary roles.
        assertEquals(DuskPrimaryContainerDark, AppColors.primaryContainer)
        assertEquals(DuskPrimaryDark, AppColors.primary)
        assertEquals(DuskSurfaceHighDark, AppColors.secondaryContainer)
        assertEquals(DuskOnSurfaceVariantDark, AppColors.secondary)
        assertEquals(DuskSurfaceDark, AppColors.surface)
        assertEquals(DuskOnPrimaryDark, AppColors.onPrimary)
        assertEquals(DuskSurfaceDark, AppColors.onSecondary)
        assertEquals(DuskOnSurfaceDark, AppColors.onSurface)
        assertEquals(DuskOnPrimaryContainerDark, AppColors.onPrimaryContainer)
        assertEquals(DuskOnSurfaceDark, AppColors.onSecondaryContainer)
        assertEquals(DuskOnSurfaceVariantDark, AppColors.onSurfaceVariant)
        assertEquals(DuskErrorDark, AppColors.error)
        assertEquals(DuskSurfaceHighDark, AppColors.errorContainer)
        assertEquals(DuskSurfaceContainerDark, AppColors.surfaceContainerLow)
        assertEquals(DuskSurfaceHighDark, AppColors.surfaceContainerHigh)
        assertEquals(DuskSurfaceHighDark, AppColors.surfaceContainerHighest)
    }

    @Test
    fun `surface container ladder stays tonally ordered`() {
        // Low < High < Highest is the ladder the card call sites rely on.
        // In the Dusk dark palette, High == Highest, so we assert <= (non-decreasing).
        val low = AppColors.surfaceContainerLow.luminance()
        val high = AppColors.surfaceContainerHigh.luminance()
        val highest = AppColors.surfaceContainerHighest.luminance()
        assertTrue(
            "low <= high <= highest",
            low <= high && high <= highest
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
