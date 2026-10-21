package com.telegramdrive.uploader.core.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * Dynamic Color Engine - Port of the mockup's color extraction & theme generation.
 *
 * The mockup (drive-uploader-ui.html) uses HSV clustering (24 bins) weighted by
 * saturation * brightness to find the dominant color, converts to CIELAB via
 * cube-root scaling (OKLab-approximate), then generates a full theme from L, C, H.
 *
 * This Kotlin implementation replicates that algorithm for Android.
 *
 * Usage:
 *   val engine = DynamicColorEngine()
 *   val colorScheme = engine.generateSchemeFromBitmap(bitmap)
 *   // or use the ViewModel for reactive UI
 */
class DynamicColorEngine {

    // ─── HSV Clustering ──────────────────────────────────────────────────────

    private val HUE_BINS = 24 // matches mockup's k = Math.floor(h * 4) % 24

    data class HsvBin(
        var weight: Float = 0f,
        var sumR: Float = 0f,
        var sumG: Float = 0f,
        var sumB: Float = 0f
    )

    /**
     * Extract dominant hue & chroma from a bitmap.
     *
     * @param bitmap Source image (downsampled for performance)
     * @return Pair(hueDegrees 0..360, chroma 0..1) or null if no dominant color
     */
    fun extractDominantHueChroma(bitmap: Bitmap): Pair<Float, Float>? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val bins = Array(HUE_BINS) { HsvBin() }

        for (px in pixels) {
            val r = ((px shr 16) and 0xFF) / 255f
            val g = ((px shr 8) and 0xFF) / 255f
            val b = (px and 0xFF) / 255f

            val maxC = maxOf(r, g, b)
            val minC = minOf(r, g, b)
            val delta = maxC - minC

            // Skip low-saturation / low-brightness pixels (mockup: s<.25||mx<.25)
            if (delta == 0f || maxC < 0.25f || delta / maxC < 0.25f) continue

            // Hue in 0..6
            val hue6 = when (maxC) {
                r -> ((g - b) / delta + 6f) % 6f
                g -> (b - r) / delta + 2f
                else -> (r - g) / delta + 4f
            }

            // Bin index 0..23 (mockup: Math.floor(h * 4) % 24)
            val binIndex = (hue6 * 4f).toInt() % HUE_BINS
            val satWeight = delta * maxC // s * mx

            val bin = bins[binIndex]
            bin.weight += satWeight
            bin.sumR += r * satWeight
            bin.sumG += g * satWeight
            bin.sumB += b * satWeight
        }

        // Find best bin
        var bestBin = bins[0]
        for (bin in bins) {
            if (bin.weight > bestBin.weight) bestBin = bin
        }

        if (bestBin.weight == 0f) return null

        val avgR = bestBin.sumR / bestBin.weight
        val avgG = bestBin.sumG / bestBin.weight
        val avgB = bestBin.sumB / bestBin.weight

        // Convert to LCH via OKLab-approximate (cube-root method from mockup)
        val (l, c, h) = rgbToLch(avgR, avgG, avgB)
        return Pair(h.toFloat(), c.toFloat())
    }

    // ─── Color Space Conversions ─────────────────────────────────────────────

    /** Linearize sRGB component (mockup's lin()) */
    private fun lin(c: Double): Double {
        return if (c <= 0.04045) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }

    /** RGB → OKLCH (mockup's r2l + hex reverse) */
    private fun rgbToLch(r: Float, g: Float, b: Float): Triple<Double, Double, Double> {
        val rl = lin(r.toDouble())
        val gl = lin(g.toDouble())
        val bl = lin(b.toDouble())

        // OKLab forward (approximate - mockup uses a simplified matrix)
        val l = cbrt(0.4122214708 * rl + 0.5363325363 * gl + 0.0514459929 * bl)
        val m = cbrt(0.2119034982 * rl + 0.6806995451 * gl + 0.1073969566 * bl)
        val s = cbrt(0.0883024619 * rl + 0.2817188376 * gl + 0.6299787005 * bl)

        val a = 1.9779984951 * l - 2.428592205 * m + 0.4505937099 * s
        val b_ = 0.0259040371 * l + 0.7827717662 * m - 0.808675766 * s

        val C = hypot(a, b_)
        val H = atan2(b_, a) * 180.0 / PI
        val Hdeg = if (H < 0) H + 360.0 else H
        return Triple(l.toDouble(), C.toDouble(), Hdeg)
    }

    /** OKLCH → sRGB hex (mockup's hex()) */
    private fun lchToRgb(L: Double, C: Double, H: Double): Int {
        val hRad = H * PI / 180.0
        val a = C * cos(hRad)
        val b_ = C * sin(hRad)

        val l_ = L + 0.3963377774 * a + 0.2158037573 * b_
        val m_ = L - 0.1055613458 * a - 0.0638541728 * b_
        val s_ = L - 0.0894841775 * a - 1.291485548 * b_

        val l3 = l_ * l_ * l_
        val m3 = m_ * m_ * m_
        val s3 = s_ * s_ * s_

        fun e(x: Double): Int {
            val clamped = x.coerceIn(0.0, 1.0)
            return if (clamped <= 0.0031308) {
                (255 * 12.92 * clamped).toInt()
            } else {
                (255 * (1.055 * Math.pow(clamped, 1.0 / 2.4) - 0.055)).toInt()
            }
        }

        val r = e(4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3)
        val g = e(-1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3)
        val b = e(-0.0041960863 * l3 - 0.7034186147 * m3 + 1.707614701 * s3)

        return ((0xFF shl 24) or (r shl 16) or (g shl 8) or b)
    }

    // ─── Theme Generation ────────────────────────────────────────────────────

    /**
     * Generate a full Material 3 ColorScheme from hue & chroma.
     * Mirrors mockup's theme(h, c) which sets all CSS variables.
     *
     * @param hueDeg Hue in degrees 0..360
     * @param chroma Chroma 0..1 (mockup clamps to 0.1..0.17)
     * @param darkTheme Whether to generate dark or light scheme
     */
    fun generateColorScheme(
        hueDeg: Float,
        chroma: Float,
        darkTheme: Boolean
    ): Material3ColorScheme {
        val c = chroma.coerceIn(0.1f, 0.17f)

        // The mockup generates these CSS variables:
        // --p (primary), --op (on-primary), --pc (primary-container), --opc (on-primary-container)
        // --k (background), --c1 (surface), --sh (surface-high), --ln (outline)
        // --w (on-surface), --g (on-surface-variant), --e (error), --oe (on-error)

        val (primary, onPrimary, primaryContainer, onPrimaryContainer) = if (darkTheme) {
            generateDarkPrimary(hueDeg, c)
        } else {
            generateLightPrimary(hueDeg, c)
        }

        val (background, surface, surfaceContainer, surfaceHigh, outline) =
            if (darkTheme) generateDarkSurfaces(hueDeg, c)
            else generateLightSurfaces(hueDeg, c)

        val onSurface = if (darkTheme) Color.White else Color.Black
        val onSurfaceVariant = if (darkTheme) Color(0xFF8E8E8E) else Color(0xFF444444)
        val error = Color(0xFFFF8A80)
        val onError = if (darkTheme) Color(0xFF2A0A08) else Color.White

        return Material3ColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = onSurfaceVariant.copy(alpha = 0.6f),
            onSecondary = onSurface,
            secondaryContainer = surfaceContainer,
            onSecondaryContainer = onSurface,
            tertiary = error,
            onTertiary = onError,
            tertiaryContainer = surfaceContainer.copy(alpha = 0.2f),
            onTertiaryContainer = onSurface,
            background = background,
            onBackground = onSurface,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceContainer,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainerLowest = background,
            surfaceContainerLow = surface,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHigh,
            error = error,
            onError = onError,
            errorContainer = surfaceContainer.copy(alpha = 0.3f),
            onErrorContainer = onSurface,
            outline = outline,
            outlineVariant = outline.copy(alpha = 0.5f)
        )
    }

    private fun generateDarkPrimary(hueDeg: Float, chroma: Float): List<Color> {
        // Mockup dark: --p: L=0.8, C=c, H=h  → --op: L=0.28, C=c*0.7  → --pc: L=0.4, C=c*0.85  → --opc: L=0.92, C=c*0.3
        val primary = lchToColor(0.8, chroma.toDouble(), hueDeg.toDouble())
        val onPrimary = lchToColor(0.28, (chroma * 0.7).toDouble(), hueDeg.toDouble())
        val primaryContainer = lchToColor(0.4, (chroma * 0.85).toDouble(), hueDeg.toDouble())
        val onPrimaryContainer = lchToColor(0.92, (chroma * 0.3).toDouble(), hueDeg.toDouble())
        return listOf(primary, onPrimary, primaryContainer, onPrimaryContainer)
    }

    private fun generateLightPrimary(hueDeg: Float, chroma: Float): List<Color> {
        // Mockup light: --p: L=0.5, C=c  → --op: L=0.99, C=0.01  → --pc: L=0.9, C=c*0.35  → --opc: L=0.28, C=c*0.7
        val primary = lchToColor(0.5, chroma.toDouble(), hueDeg.toDouble())
        val onPrimary = lchToColor(0.99, 0.01, hueDeg.toDouble())
        val primaryContainer = lchToColor(0.9, (chroma * 0.35).toDouble(), hueDeg.toDouble())
        val onPrimaryContainer = lchToColor(0.28, (chroma * 0.7).toDouble(), hueDeg.toDouble())
        return listOf(primary, onPrimary, primaryContainer, onPrimaryContainer)
    }

    private fun generateDarkSurfaces(hueDeg: Float, chroma: Float): List<Color> {
        // --k: L=0.15, C=0.014  --c1: L=0.2, C=0.022  --sh: L=0.29, C=0.03  --ln: L=0.3, C=0.025  --w: L=0.94, C=0.01  --g: L=0.74, C=0.03
        val bg = lchToColor(0.15, 0.014, hueDeg.toDouble())
        val surface = lchToColor(0.15, 0.014, hueDeg.toDouble()) // same as bg in mockup
        val surfaceContainer = lchToColor(0.2, 0.022, hueDeg.toDouble())
        val surfaceHigh = lchToColor(0.29, 0.03, hueDeg.toDouble())
        val outline = lchToColor(0.3, 0.025, hueDeg.toDouble())
        return listOf(bg, surface, surfaceContainer, surfaceHigh, outline)
    }

    private fun generateLightSurfaces(hueDeg: Float, chroma: Float): List<Color> {
        // --k: L=0.98, C=0.006  --c1: L=0.95, C=0.012  --sh: L=0.88, C=0.02  --ln: L=0.86, C=0.015  --w: L=0.2, C=0.012  --g: L=0.45, C=0.025
        val bg = lchToColor(0.98, 0.006, hueDeg.toDouble())
        val surface = lchToColor(0.98, 0.006, hueDeg.toDouble())
        val surfaceContainer = lchToColor(0.95, 0.012, hueDeg.toDouble())
        val surfaceHigh = lchToColor(0.88, 0.02, hueDeg.toDouble())
        val outline = lchToColor(0.86, 0.015, hueDeg.toDouble())
        return listOf(bg, surface, surfaceContainer, surfaceHigh, outline)
    }

    private fun lchToColor(L: Double, C: Double, H: Double): Color {
        val argb = lchToRgb(L, C, H)
        return Color(argb)
    }
}

/**
 * Simple data class mirroring Material 3 ColorScheme fields we need.
 * Used to pass generated schemes between engine and theme composable.
 */
data class Material3ColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val outline: Color,
    val outlineVariant: Color
) {
    fun toMaterial3ColorScheme(darkTheme: Boolean): androidx.compose.material3.ColorScheme {
        if (darkTheme) {
            return androidx.compose.material3.darkColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = Color.Black,
                surfaceTint = primary,
                inverseSurface = onSurface,
                inverseOnSurface = surface,
                inversePrimary = primaryContainer,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
            )
        } else {
            return androidx.compose.material3.lightColorScheme(
                primary = primary,
                onPrimary = onPrimary,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                secondary = secondary,
                onSecondary = onSecondary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                tertiary = tertiary,
                onTertiary = onTertiary,
                tertiaryContainer = tertiaryContainer,
                onTertiaryContainer = onTertiaryContainer,
                background = background,
                onBackground = onBackground,
                surface = surface,
                onSurface = onSurface,
                surfaceVariant = surfaceVariant,
                onSurfaceVariant = onSurfaceVariant,
                outline = outline,
                outlineVariant = outlineVariant,
                scrim = Color.Black,
                surfaceTint = primary,
                inverseSurface = onSurface,
                inverseOnSurface = surface,
                inversePrimary = primaryContainer,
                error = error,
                onError = onError,
                errorContainer = errorContainer,
                onErrorContainer = onErrorContainer,
            )
        }
    }
}