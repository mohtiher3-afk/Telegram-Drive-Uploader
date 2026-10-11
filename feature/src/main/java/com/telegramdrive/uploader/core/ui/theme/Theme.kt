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
 * Dusk palette from hermes-package/tokens.json.
 * Fallback scheme when dynamic color is disabled or below Android 12.
 */
private val DuskDarkColorScheme = darkColorScheme(
    primary = DuskPrimaryDark,
    onPrimary = DuskOnPrimaryDark,
    primaryContainer = DuskPrimaryContainerDark,
    onPrimaryContainer = DuskOnPrimaryContainerDark,
    secondary = DuskOnSurfaceVariantDark,
    onSecondary = DuskSurfaceDark,
    secondaryContainer = DuskSurfaceHighDark,
    onSecondaryContainer = DuskOnSurfaceDark,
    tertiary = DuskTertiaryDark,
    onTertiary = DuskSurfaceDark,
    tertiaryContainer = DuskSurfaceHighDark,
    onTertiaryContainer = DuskOnSurfaceDark,
    background = DuskSurfaceDark,
    onBackground = DuskOnSurfaceDark,
    surface = DuskSurfaceDark,
    onSurface = DuskOnSurfaceDark,
    surfaceVariant = DuskSurfaceContainerDark,
    onSurfaceVariant = DuskOnSurfaceVariantDark,
    surfaceContainerLowest = DuskSurfaceDark,
    surfaceContainerLow = DuskSurfaceContainerDark,
    surfaceContainer = DuskSurfaceContainerDark,
    surfaceContainerHigh = DuskSurfaceHighDark,
    surfaceContainerHighest = DuskSurfaceHighDark,
    error = DuskErrorDark,
    onError = DuskSurfaceDark,
    errorContainer = DuskSurfaceHighDark,
    onErrorContainer = DuskOnSurfaceDark,
    outline = DuskOutlineDark,
    outlineVariant = DuskOutlineDark
)

private val DuskLightColorScheme = lightColorScheme(
    primary = DuskPrimaryLight,
    onPrimary = DuskOnPrimaryLight,
    primaryContainer = DuskPrimaryContainerLight,
    onPrimaryContainer = DuskOnPrimaryContainerLight,
    secondary = DuskOnSurfaceVariantLight,
    onSecondary = DuskSurfaceLight,
    secondaryContainer = DuskSurfaceHighLight,
    onSecondaryContainer = DuskOnSurfaceLight,
    tertiary = DuskTertiaryLight,
    onTertiary = DuskSurfaceLight,
    tertiaryContainer = DuskSurfaceHighLight,
    onTertiaryContainer = DuskOnSurfaceLight,
    background = DuskSurfaceLight,
    onBackground = DuskOnSurfaceLight,
    surface = DuskSurfaceLight,
    onSurface = DuskOnSurfaceLight,
    surfaceVariant = DuskSurfaceContainerLight,
    onSurfaceVariant = DuskOnSurfaceVariantLight,
    surfaceContainerLowest = DuskSurfaceLight,
    surfaceContainerLow = DuskSurfaceContainerLight,
    surfaceContainer = DuskSurfaceContainerLight,
    surfaceContainerHigh = DuskSurfaceHighLight,
    surfaceContainerHighest = DuskSurfaceHighLight,
    error = DuskErrorLight,
    onError = DuskSurfaceLight,
    errorContainer = DuskSurfaceHighLight,
    onErrorContainer = DuskOnSurfaceLight,
    outline = DuskOutlineLight,
    outlineVariant = DuskOutlineLight
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
        darkTheme -> DuskDarkColorScheme
        else -> DuskLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
