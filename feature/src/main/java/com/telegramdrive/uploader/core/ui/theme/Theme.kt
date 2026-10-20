package com.telegramdrive.uploader.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Coral palette matching the design spec (--p: #ff6b61).
 * Fallback scheme when dynamic color is disabled or below Android 12.
 */
private val CoralDarkColorScheme = darkColorScheme(
    primary = CoralPrimaryDark,
    onPrimary = CoralOnPrimaryDark,
    primaryContainer = CoralPrimaryContainerDark,
    onPrimaryContainer = CoralOnPrimaryContainerDark,
    secondary = CoralOnSurfaceVariantDark,
    onSecondary = CoralSurfaceDark,
    secondaryContainer = CoralSurfaceHighDark,
    onSecondaryContainer = CoralOnSurfaceDark,
    tertiary = CoralTertiaryDark,
    onTertiary = CoralSurfaceDark,
    tertiaryContainer = CoralSurfaceHighDark,
    onTertiaryContainer = CoralOnSurfaceDark,
    background = CoralSurfaceDark,
    onBackground = CoralOnSurfaceDark,
    surface = CoralSurfaceDark,
    onSurface = CoralOnSurfaceDark,
    surfaceVariant = CoralSurfaceContainerDark,
    onSurfaceVariant = CoralOnSurfaceVariantDark,
    surfaceContainerLowest = CoralSurfaceDark,
    surfaceContainerLow = CoralSurfaceContainerDark,
    surfaceContainer = CoralSurfaceContainerDark,
    surfaceContainerHigh = CoralSurfaceHighDark,
    surfaceContainerHighest = CoralSurfaceHighDark,
    error = CoralErrorDark,
    onError = CoralSurfaceDark,
    errorContainer = CoralSurfaceHighDark,
    onErrorContainer = CoralOnSurfaceDark,
    outline = CoralOutlineDark,
    outlineVariant = CoralOutlineDark
)

private val CoralLightColorScheme = lightColorScheme(
    primary = CoralPrimaryLight,
    onPrimary = CoralOnPrimaryLight,
    primaryContainer = CoralPrimaryContainerLight,
    onPrimaryContainer = CoralOnPrimaryContainerLight,
    secondary = CoralOnSurfaceVariantLight,
    onSecondary = CoralSurfaceLight,
    secondaryContainer = CoralSurfaceHighLight,
    onSecondaryContainer = CoralOnSurfaceLight,
    tertiary = CoralTertiaryLight,
    onTertiary = CoralSurfaceLight,
    tertiaryContainer = CoralSurfaceHighLight,
    onTertiaryContainer = CoralOnSurfaceLight,
    background = CoralSurfaceLight,
    onBackground = CoralOnSurfaceLight,
    surface = CoralSurfaceLight,
    onSurface = CoralOnSurfaceLight,
    surfaceVariant = CoralSurfaceContainerLight,
    onSurfaceVariant = CoralOnSurfaceVariantLight,
    surfaceContainerLowest = CoralSurfaceLight,
    surfaceContainerLow = CoralSurfaceContainerLight,
    surfaceContainer = CoralSurfaceContainerLight,
    surfaceContainerHigh = CoralSurfaceHighLight,
    surfaceContainerHighest = CoralSurfaceHighLight,
    error = CoralErrorLight,
    onError = CoralSurfaceLight,
    errorContainer = CoralSurfaceHighLight,
    onErrorContainer = CoralOnSurfaceLight,
    outline = CoralOutlineLight,
    outlineVariant = CoralOutlineLight
)

/**
 * App theme with Material You dynamic color support.
 *
 * On Android 12+, when [dynamicColorEnabled] is true, the system wallpaper-derived
 * color scheme is used. Otherwise (or on older Android), the Dusk palette from
 * tokens.json is the fallback.
 *
 * Typography uses Doto for display/headlines and Space Mono for body/labels,
 * with IBM Plex Sans Arabic as the Arabic fallback (see Type.kt).
 */
@Composable
fun TelegramDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColorEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CoralDarkColorScheme
        else -> CoralLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
