package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Guards the token surface every screen compiles against.
 * Validates that shared layout tokens (spacing, radius, content-width) are consistent.
 */
class DesignTokensTest {

    @Test
    fun `grouped token objects are reachable through the container`() {
        assertSame(AppSpacing, DesignTokens.AppSpacing)
        assertSame(AppContentWidth, DesignTokens.AppContentWidth)
    }

    @Test
    fun `flat aliases keep their grouped values`() {
        assertEquals(AppSpacing.medium, DesignTokens.spacingM)
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
