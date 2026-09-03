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
 * Tideglass Relay semantic scheme: Seafoam action, Horizon context, Coral hero light,
 * and Harbor Ink/Salt Paper foundations. Dynamic color remains disabled by MainActivity for brand consistency.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1D2),
    onPrimary = Color(0xFF5D0031),
    primaryContainer = Color(0xFF821A4D),
    onPrimaryContainer = Color(0xFFFFD9E7),
    secondary = Color(0xFFCDBDFF),
    onSecondary = Color(0xFF331062),
    secondaryContainer = Color(0xFF4A2B80),
    onSecondaryContainer = Color(0xFFE9DCFF),
    tertiary = Color(0xFFFFC86B),
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF653F00),
    onTertiaryContainer = Color(0xFFFFDEA6),
    background = TideHarborInk,
    onBackground = Color(0xFFFDECFF),
    surface = Color(0xF2140718),
    onSurface = Color(0xFFFDECFF),
    surfaceVariant = Color(0xFF4A3A55),
    onSurfaceVariant = Color(0xFFD9C2E4),
    surfaceContainerLowest = Color(0xFF0D0312),
    surfaceContainerLow = Color(0xFF1E1230),
    surfaceContainer = Color(0xFF291B3D),
    surfaceContainerHigh = Color(0xFF34234A),
    surfaceContainerHighest = Color(0xFF3F2D56),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFFB5A0C2),
    outlineVariant = Color(0xFF46364F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB02772),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E7),
    onPrimaryContainer = Color(0xFF3E0024),
    secondary = Color(0xFF5C3FA2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DCFF),
    onSecondaryContainer = Color(0xFF1D0B52),
    tertiary = Color(0xFF9A5B00),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDEA6),
    onTertiaryContainer = Color(0xFF321A00),
    background = TideSaltPaper,
    onBackground = Color(0xFF241321),
    surface = TideSaltPaper,
    onSurface = Color(0xFF241321),
    surfaceVariant = Color(0xFFF1DCF0),
    onSurfaceVariant = Color(0xFF51434E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF4FA),
    surfaceContainer = Color(0xFFF5E7F4),
    surfaceContainerHigh = Color(0xFFEFDEEE),
    surfaceContainerHighest = Color(0xFFE9DAE8),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF77727C),
    outlineVariant = Color(0xFFC9C0C9)
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
