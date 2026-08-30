package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs
import kotlin.math.floor

/** Curated and custom primary-signal choices; semantic success, warning, and error roles stay untouched. */
enum class GlowColorPreset(
    val storageValue: String,
    private val dark: GlowPrimaryColors?,
    private val light: GlowPrimaryColors?
) {
    SEAFOAM(
        storageValue = "Seafoam",
        dark = GlowPrimaryColors(Color(0xFFA5F6D2), Color(0xFF073827), Color(0xFF00533B), Color(0xFFC2FFE0)),
        light = GlowPrimaryColors(Color(0xFF006C4D), Color.White, Color(0xFF8DF9C9), Color(0xFF002114))
    ),
    COBALT(
        storageValue = "Cobalt",
        dark = GlowPrimaryColors(Color(0xFFB8C4FF), Color(0xFF102255), Color(0xE6364F9D), Color(0xFFDCE5FF)),
        light = GlowPrimaryColors(Color(0xFF314D9E), Color.White, Color(0xFFDCE5FF), Color(0xFF001A5B))
    ),
    LIME(
        storageValue = "Lime",
        dark = GlowPrimaryColors(Color(0xFFCBEA4E), Color(0xFF1B2109), Color(0xE34A5C0F), Color(0xFFF0FFC3)),
        light = GlowPrimaryColors(Color(0xFF4E6700), Color.White, Color(0xFFD0F2A1), Color(0xFF152000))
    ),
    CYAN(
        storageValue = "Cyan",
        dark = GlowPrimaryColors(Color(0xFF8EDCFF), Color(0xFF003549), Color(0xE7005673), Color(0xFFC4EDFF)),
        light = GlowPrimaryColors(Color(0xFF006782), Color.White, Color(0xFFBBEAFF), Color(0xFF003548))
    ),
    VIOLET(
        storageValue = "Violet",
        dark = GlowPrimaryColors(Color(0xFFE1B6FF), Color(0xFF4B006A), Color(0xE58C27B6), Color(0xFFFFD8F3)),
        light = GlowPrimaryColors(Color(0xFF794A95), Color.White, Color(0xFFF7D8FF), Color(0xFF2F003F))
    ),
    CUSTOM(storageValue = "Custom", dark = null, light = null);

    fun applyTo(base: ColorScheme, darkTheme: Boolean, customHex: String = GlowColorCodec.DEFAULT_HEX): ColorScheme {
        val colors = if (this == CUSTOM) {
            GlowColorCodec.primaryColorsFor(GlowColorCodec.colorFromHex(customHex), darkTheme)
        } else if (darkTheme) {
            requireNotNull(dark)
        } else {
            requireNotNull(light)
        }
        return base.copy(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer
        )
    }

    fun swatchColor(customHex: String = GlowColorCodec.DEFAULT_HEX): Color =
        if (this == CUSTOM) GlowColorCodec.colorFromHex(customHex) else requireNotNull(dark).primary

    companion object {
        fun fromStorage(value: String?): GlowColorPreset =
            entries.firstOrNull { it.storageValue == value } ?: SEAFOAM
    }
}

/** Storage-safe hex parsing and primary-role derivation for a user-supplied Glow color. */
object GlowColorCodec {
    const val DEFAULT_HEX = "69D6B5"

    fun normalizeHex(value: String?): String {
        val normalized = value.orEmpty().trim().removePrefix("#")
        return if (normalized.matches(Regex("[0-9A-Fa-f]{6}"))) normalized.uppercase() else DEFAULT_HEX
    }

    fun colorFromHex(value: String?): Color {
        val hex = normalizeHex(value).toLong(16)
        return Color(
            red = ((hex shr 16) and 0xFF) / 255f,
            green = ((hex shr 8) and 0xFF) / 255f,
            blue = (hex and 0xFF) / 255f
        )
    }

    fun hexFromColor(color: Color): String = "%02X%02X%02X".format(
        (color.red * 255).toInt().coerceIn(0, 255),
        (color.green * 255).toInt().coerceIn(0, 255),
        (color.blue * 255).toInt().coerceIn(0, 255)
    )

    fun colorFromHsv(hue: Float, saturation: Float, value: Float): Color {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val c = v * s
        val x = c * (1 - abs((h / 60f) % 2 - 1))
        val m = v - c
        val (r, g, b) = when (floor(h / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color(r + m, g + m, b + m)
    }

    fun hsvFromColor(color: Color): GlowHsv {
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        val delta = max - min
        val hue = when {
            delta == 0f -> 0f
            max == color.red -> 60f * (((color.green - color.blue) / delta) % 6f)
            max == color.green -> 60f * (((color.blue - color.red) / delta) + 2f)
            else -> 60f * (((color.red - color.green) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }
        val saturation = if (max == 0f) 0f else delta / max
        return GlowHsv(hue, saturation, max)
    }

    internal fun primaryColorsFor(source: Color, darkTheme: Boolean): GlowPrimaryColors {
        return if (darkTheme) {
            val initialPrimary = if (source.luminance() < 0.22f) source.mix(Color.White, 0.42f) else source
            val primary = if (initialPrimary.contrastRatio(TideHarborInk) >= 4.5f) {
                initialPrimary
            } else {
                initialPrimary.ensureContrastWith(Color.White)
            }
            GlowPrimaryColors(
                primary = primary,
                onPrimary = primary.bestForeground(),
                primaryContainer = source.mix(TideHarborInk, 0.70f),
                onPrimaryContainer = Color(0xFFF3F6FF)
            )
        } else {
            val initialPrimary = if (source.luminance() > 0.42f) source.mix(Color.Black, 0.54f) else source
            val primary = initialPrimary.ensureContrastWith(Color.White)
            GlowPrimaryColors(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = source.mix(Color.White, 0.78f),
                onPrimaryContainer = Color(0xFF10141D)
            )
        }
    }

    private fun Color.mix(other: Color, amount: Float): Color {
        val t = amount.coerceIn(0f, 1f)
        return Color(
            red = red + ((other.red - red) * t),
            green = green + ((other.green - green) * t),
            blue = blue + ((other.blue - blue) * t),
            alpha = alpha + ((other.alpha - alpha) * t)
        )
    }

    private fun Color.ensureContrastWith(foreground: Color): Color {
        var candidate = this
        repeat(20) {
            if (candidate.contrastRatio(foreground) >= 4.5f) return candidate
            candidate = candidate.mix(Color.Black, 0.08f)
        }
        return candidate
    }

    private fun Color.bestForeground(): Color {
        val darkForeground = TideHarborInk
        return if (contrastRatio(darkForeground) >= contrastRatio(Color.White)) darkForeground else Color.White
    }

    private fun Color.contrastRatio(other: Color): Float {
        val lighter = maxOf(luminance(), other.luminance())
        val darker = minOf(luminance(), other.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}

data class GlowHsv(val hue: Float, val saturation: Float, val value: Float)

internal data class GlowPrimaryColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)
