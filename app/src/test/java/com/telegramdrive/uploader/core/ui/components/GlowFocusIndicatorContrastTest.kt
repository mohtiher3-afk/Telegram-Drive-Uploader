package com.telegramdrive.uploader.core.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class GlowFocusIndicatorContrastTest {

    @Test
    fun `focus outline retains at least three to one contrast against base surfaces`() {
        assertTrue(contrastRatio(Color(0xFFAAB5C7), Color(0xFF0B101B)) >= 3f)
        assertTrue(contrastRatio(Color(0xFF717987), Color(0xFFF9FAFF)) >= 3f)
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
