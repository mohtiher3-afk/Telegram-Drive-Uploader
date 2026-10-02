package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
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
 * aliases present, and pin the container roles to the light scheme in Theme.kt so the
 * duplicated palette cannot drift away from the theme unnoticed.
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
    fun `container roles mirror the light scheme values`() {
        // Only Theme.kt defines these roles; the screens read the AppColors copy,
        // so the copy has to match the light scheme exactly.
        assertEquals(Color(0xFF0A1A68), AppColors.onPrimaryContainer)
        assertEquals(Color(0xFF181849), AppColors.onSecondaryContainer)
        assertEquals(Color(0xFF46464F), AppColors.onSurfaceVariant)
        assertEquals(Color(0xFFFFDAD6), AppColors.errorContainer)
    }

    @Test
    fun `secondary container steps stay tonally ordered`() {
        // Low < base < High < Highest is the ladder the card call sites rely on, so
        // assert the relationship instead of pinning duplicate literals here.
        val low = AppColors.secondaryContainerLow.luminance()
        val base = AppColors.secondaryContainer.luminance()
        val high = AppColors.secondaryContainerHigh.luminance()
        val highest = AppColors.secondaryContainerHighest.luminance()
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