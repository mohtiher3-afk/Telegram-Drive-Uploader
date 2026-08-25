package com.telegramdrive.uploader.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
 * Pinterest-inspired semantic scheme: blue-black glass, cobalt action, and teal
 * completion. Dynamic color remains disabled by MainActivity for brand consistency.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8C4FF),
    onPrimary = Color(0xFF102255),
    primaryContainer = Color(0xE6364F9D),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = Color(0xFFB7C5D9),
    onSecondary = Color(0xFF213044),
    secondaryContainer = Color(0xE6324358),
    onSecondaryContainer = Color(0xFFDDE7F6),
    tertiary = Color(0xFFA6EBD1),
    onTertiary = Color(0xFF00382D),
    tertiaryContainer = Color(0xE6005B4B),
    onTertiaryContainer = Color(0xFFC6FFE8),
    background = Color(0xFF0B101B),
    onBackground = Color(0xFFF1F4FF),
    surface = Color(0xF20B101B),
    onSurface = Color(0xFFF1F4FF),
    surfaceVariant = Color(0xE63A4960),
    onSurfaceVariant = Color(0xFFC2CAD8),
    surfaceContainerLowest = Color(0xE60E1420),
    surfaceContainerLow = Color(0xDA131B29),
    surfaceContainer = Color(0xDE182230),
    surfaceContainerHigh = Color(0xE6243040),
    surfaceContainerHighest = Color(0xEC2D3A4C),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFFAAB5C7),
    outlineVariant = Color(0xFF3A4960)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF314D9E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5FF),
    onPrimaryContainer = Color(0xFF001A5B),
    secondary = Color(0xFF526275),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E7F9),
    onSecondaryContainer = Color(0xFF0E2134),
    tertiary = Color(0xFF006B59),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB0F3DF),
    onTertiaryContainer = Color(0xFF00382E),
    background = Color(0xFFF9FAFF),
    onBackground = Color(0xFF181C26),
    surface = Color(0xFFF9FAFF),
    onSurface = Color(0xFF181C26),
    surfaceVariant = Color(0xFFE0E7F1),
    onSurfaceVariant = Color(0xFF424955),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F7FD),
    surfaceContainer = Color(0xFFF1F5FC),
    surfaceContainerHigh = Color(0xFFEAF0F8),
    surfaceContainerHighest = Color(0xFFE4EBF5),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF717987),
    outlineVariant = Color(0xFFC2CAD6)
)

/** Varied shapes create hierarchy without turning every surface into a card. */
private val ExpressiveShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(30.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(38.dp)
)

@Composable
fun TelegramDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    glowColorPreset: GlowColorPreset = GlowColorPreset.COBALT,
    customGlowHex: String = GlowColorCodec.DEFAULT_HEX,
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val colorScheme = glowColorPreset.applyTo(baseColorScheme, darkTheme, customGlowHex)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
