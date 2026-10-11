package com.telegramdrive.uploader.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Material You (Dynamic Color) support with brand-preserving options.
 * 
 * Dynamic Color Strategy:
 * - If `dynamicColorStrategy = Full` on Android 12+, use system dynamic scheme
 * - Brand colors applied only to secondary/tertiary roles (preserves dynamic primary)
 * - GlowColorPreset applies to secondary/tertiary/surface roles, NOT primary
 * - Fallback to Calm Material schemes on older Android or when disabled
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4A3FF),
    onPrimary = Color(0xFF351551),
    primaryContainer = Color(0xFF58317E),
    onPrimaryContainer = Color(0xFFEBDEFD),
    secondary = Color(0xFFCFC3D8),
    onSecondary = Color(0xFF362D41),
    secondaryContainer = Color(0xFF4D4358),
    onSecondaryContainer = Color(0xFFEBDEF4),
    tertiary = Color(0xFFFCA9BB),
    onTertiary = Color(0xFF4E1F2C),
    tertiaryContainer = Color(0xFF6B3442),
    onTertiaryContainer = Color(0xFFFFD9E0),
    background = Color(0xFF0C0A10),
    onBackground = Color(0xFFEDEAF1),
    surface = Color(0xFF0C0A10),
    onSurface = Color(0xFFEDEAF1),
    surfaceVariant = Color(0xFF4A4453),
    onSurfaceVariant = Color(0xFFAFA6BA),
    surfaceContainerLowest = Color(0xFF0C0A10),
    surfaceContainerLow = Color(0xFF18141E),
    surfaceContainer = Color(0xFF18141E),
    surfaceContainerHigh = Color(0xFF2F2837),
    surfaceContainerHighest = Color(0xFF3A3244),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF312B38),
    outlineVariant = Color(0xFF312B38)
)

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7847A6),
    onPrimary = Color(0xFFFDFAFF),
    primaryContainer = Color(0xFFE6D6FB),
    onPrimaryContainer = Color(0xFF351551),
    secondary = Color(0xFF635B70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF6),
    onSecondaryContainer = Color(0xFF1F1829),
    tertiary = Color(0xFF92495B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E0),
    onTertiaryContainer = Color(0xFF3B0716),
    background = Color(0xFFF9F7FC),
    onBackground = Color(0xFF17151A),
    surface = Color(0xFFF9F7FC),
    onSurface = Color(0xFF17151A),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF585261),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F2FA),
    surfaceContainer = Color(0xFFF0EDF5),
    surfaceContainerHigh = Color(0xFFE4DFEC),
    surfaceContainerHighest = Color(0xFFDAD4E2),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFFD3CFD9),
    outlineVariant = Color(0xFFD3CFD9)
)

/**
 * Dynamic color strategy enum for granular control
 */
enum class DynamicColorStrategy {
    /** Full dynamic color (primary from system, brand on secondary/tertiary) */
    Full,
    /** Dynamic primary only, brand colors on secondary/tertiary/surface */
    BrandAccented,
    /** Static brand colors only (legacy behavior) */
    StaticBrand
}

/**
 * Applies brand accent colors to a dynamic color scheme while preserving dynamic primary.
 * Only modifies secondary, tertiary, and surface variant roles.
 */
private fun ColorScheme.withBrandAccents(
    darkTheme: Boolean,
    preset: GlowColorPreset,
    customHex: String = GlowColorCodec.DEFAULT_HEX
): ColorScheme {
    val colors = when {
        preset == GlowColorPreset.CUSTOM -> 
            GlowColorCodec.primaryColorsFor(GlowColorCodec.colorFromHex(customHex), darkTheme)
        darkTheme -> preset.dark ?: return this
        else -> preset.light ?: return this
    }
    
    return this.copy(
        // Apply brand colors to secondary/tertiary roles (preserves dynamic primary)
        secondary = colors.primary,
        onSecondary = colors.onPrimary,
        secondaryContainer = colors.primaryContainer,
        onSecondaryContainer = colors.onPrimaryContainer,
        tertiary = colors.primary,
        onTertiary = colors.onPrimary,
        tertiaryContainer = colors.primaryContainer,
        onTertiaryContainer = colors.onPrimaryContainer,
        // Subtle brand influence on surface variants
        surfaceVariant = if (darkTheme)
            androidx.compose.ui.graphics.lerp(surfaceVariant, colors.primary, 0.08f)
            else androidx.compose.ui.graphics.lerp(surfaceVariant, colors.primary, 0.05f),
        surfaceContainerHigh = if (darkTheme)
            androidx.compose.ui.graphics.lerp(surfaceContainerHigh, colors.primary, 0.05f)
            else androidx.compose.ui.graphics.lerp(surfaceContainerHigh, colors.primary, 0.03f),
        surfaceContainerHighest = if (darkTheme)
            androidx.compose.ui.graphics.lerp(surfaceContainerHighest, colors.primary, 0.03f)
            else androidx.compose.ui.graphics.lerp(surfaceContainerHighest, colors.primary, 0.02f)
    )
}

/** Clean, moderate shapes create hierarchy without oversized or decorative corners. */
private val ExpressiveShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun TelegramDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // StaticBrand is the default because it is the only strategy that assigns the
    // caller's glowColorPreset to primary. Defaulting to BrandAccented would leave
    // glowColorPreset and customGlowHex affecting only secondary/tertiary/surface
    // roles, so a caller that forgot this argument would render with an apparently
    // ignored colour setting.
    dynamicColorStrategy: DynamicColorStrategy = DynamicColorStrategy.StaticBrand,
    glowColorPreset: GlowColorPreset = GlowColorPreset.SEAFOAM,
    customGlowHex: String = GlowColorCodec.DEFAULT_HEX,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColorStrategy == DynamicColorStrategy.Full && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dynamicColorStrategy == DynamicColorStrategy.BrandAccented && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            dynamicScheme.withBrandAccents(darkTheme, glowColorPreset, customGlowHex)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val finalColorScheme = when (dynamicColorStrategy) {
        DynamicColorStrategy.Full -> baseColorScheme // Pure dynamic, no brand override
        DynamicColorStrategy.BrandAccented -> baseColorScheme // Already has brand accents applied
        DynamicColorStrategy.StaticBrand -> glowColorPreset.applyTo(baseColorScheme, darkTheme, customGlowHex)
    }

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
