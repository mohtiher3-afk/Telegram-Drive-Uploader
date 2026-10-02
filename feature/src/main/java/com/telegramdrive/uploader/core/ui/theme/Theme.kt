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
    primary = Color(0xFFA3E635),
    onPrimary = Color(0xFF172000),
    primaryContainer = Color(0xFF364F00),
    onPrimaryContainer = Color(0xFFDDF9A3),
    secondary = Color(0xFF8EDCFF),
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF005673),
    onSecondaryContainer = Color(0xFFC4EDFF),
    tertiary = Color(0xFFE1B6FF),
    onTertiary = Color(0xFF4B006A),
    tertiaryContainer = Color(0xFF8C27B6),
    onTertiaryContainer = Color(0xFFFFD8F3),
    background = Color(0xFF0D0D0F),
    onBackground = Color(0xFFECECEF),
    surface = Color(0xFF141418),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF3B3F4D),
    onSurfaceVariant = Color(0xFFCFD3E2),
    surfaceContainerLowest = Color(0xFF0E1015),
    surfaceContainerLow = Color(0xFF1C1E25),
    surfaceContainer = Color(0xFF23252E),
    surfaceContainerHigh = Color(0xFF2A2C35),
    surfaceContainerHighest = Color(0xFF353842),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF5A5D66),
    outlineVariant = Color(0xFF363943)
)

internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF587700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4A4),
    onPrimaryContainer = Color(0xFF172000),
    secondary = Color(0xFF006782),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBBEAFF),
    onSecondaryContainer = Color(0xFF003548),
    tertiary = Color(0xFF794A95),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF7D8FF),
    onTertiaryContainer = Color(0xFF2F003F),
    background = Color(0xFFFAF9FB),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFAF9FB),
    onSurface = Color(0xFF1C1B20),
    surfaceVariant = Color(0xFFE4E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F6FA),
    surfaceContainer = Color(0xFFF2F0F5),
    surfaceContainerHigh = Color(0xFFECE9F0),
    surfaceContainerHighest = Color(0xFFE6E3EA),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C4CE)
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
    glowColorPreset: GlowColorPreset = GlowColorPreset.LIME,
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
