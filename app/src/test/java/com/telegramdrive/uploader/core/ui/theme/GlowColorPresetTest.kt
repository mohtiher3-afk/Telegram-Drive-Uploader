package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlowColorPresetTest {

    @Test
    fun `unknown stored color falls back to cobalt`() {
        assertEquals(GlowColorPreset.COBALT, GlowColorPreset.fromStorage("Unknown"))
    }

    @Test
    fun `preset overrides primary roles but preserves tertiary completion role`() {
        val base = darkColorScheme()
        val customized = GlowColorPreset.LIME.applyTo(base, darkTheme = true)

        assertEquals(GlowColorPreset.LIME.swatchColor(), customized.primary)
        assertEquals(base.tertiary, customized.tertiary)
        assertEquals(base.error, customized.error)
    }

    @Test
    fun `invalid custom hex falls back to cobalt`() {
        assertEquals(GlowColorCodec.DEFAULT_HEX, GlowColorCodec.normalizeHex("not-a-color"))
        assertEquals(GlowColorCodec.DEFAULT_HEX, GlowColorCodec.normalizeHex(null))
    }

    @Test
    fun `custom glow overrides primary roles but preserves status roles`() {
        val base = darkColorScheme()
        val customized = GlowColorPreset.CUSTOM.applyTo(base, darkTheme = true, customHex = "FF00AA")

        assertEquals(GlowColorCodec.colorFromHex("FF00AA"), customized.primary)
        assertEquals(base.tertiary, customized.tertiary)
        assertEquals(base.error, customized.error)
    }

    @Test
    fun `custom primary and foreground retain readable contrast in dark and light themes`() {
        listOf("000000", "FFFFFF", "FF00AA", "00FFFF", "FFFF00").forEach { hex ->
            val dark = GlowColorPreset.CUSTOM.applyTo(darkColorScheme(), darkTheme = true, customHex = hex)
            val light = GlowColorPreset.CUSTOM.applyTo(lightColorScheme(), darkTheme = false, customHex = hex)

            assertTrue("dark primary contrast for $hex", contrastRatio(dark.primary, dark.onPrimary) >= 4.5f)
            assertTrue(
                "dark primary container contrast for $hex",
                contrastRatio(dark.primaryContainer, dark.onPrimaryContainer) >= 4.5f
            )
            assertTrue("light primary contrast for $hex", contrastRatio(light.primary, light.onPrimary) >= 4.5f)
            assertTrue(
                "light primary container contrast for $hex",
                contrastRatio(light.primaryContainer, light.onPrimaryContainer) >= 4.5f
            )
        }
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
