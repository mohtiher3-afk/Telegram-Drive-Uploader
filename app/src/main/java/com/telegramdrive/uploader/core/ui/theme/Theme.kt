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
 * Calm Material semantic scheme: a modern indigo action, muted violet context,
 * and neutral slate foundations. Dynamic color remains disabled by MainActivity for brand consistency.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA9B4FF),
    onPrimary = Color(0xFF0D1B5B),
    primaryContainer = Color(0xFF2B3C9E),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC4C2F5),
    onSecondary = Color(0xFF2B2A55),
    secondaryContainer = Color(0xFF41409E),
    onSecondaryContainer = Color(0xFFE3E0FF),
    tertiary = Color(0xFF96C8FF),
    onTertiary = Color(0xFF00325B),
    tertiaryContainer = Color(0xFF1F4A7A),
    onTertiaryContainer = Color(0xFFD3E7FF),
    background = TideHarborInk,
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF14161C),
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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3E56C8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF0A1A68),
    secondary = Color(0xFF5B5B8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E0FF),
    onSecondaryContainer = Color(0xFF181849),
    tertiary = Color(0xFF17699D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD0E9FF),
    onTertiaryContainer = Color(0xFF00213A),
    background = TideSaltPaper,
    onBackground = Color(0xFF1C1B20),
    surface = TideSaltPaper,
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
    dynamicColor: Boolean = true,
    glowColorPreset: GlowColorPreset = GlowColorPreset.SEAFOAM,
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
