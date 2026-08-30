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
    primary = Color(0xFFA5F6D2),
    onPrimary = Color(0xFF073827),
    primaryContainer = Color(0xFF00533B),
    onPrimaryContainer = Color(0xFFC2FFE0),
    secondary = Color(0xFFB9C5FF),
    onSecondary = Color(0xFF17275F),
    secondaryContainer = Color(0xFF3547A0),
    onSecondaryContainer = Color(0xFFE0E5FF),
    tertiary = Color(0xFFFFB59F),
    onTertiary = Color(0xFF571B0D),
    tertiaryContainer = Color(0xFF7A2E1D),
    onTertiaryContainer = Color(0xFFFFDAD2),
    background = TideHarborInk,
    onBackground = Color(0xFFF1FAF5),
    surface = Color(0xF2102128),
    onSurface = Color(0xFFF1FAF5),
    surfaceVariant = Color(0xFF3A4D52),
    onSurfaceVariant = Color(0xFFC0D1CE),
    surfaceContainerLowest = Color(0xFF0B171C),
    surfaceContainerLow = Color(0xFF14272D),
    surfaceContainer = Color(0xFF1B333A),
    surfaceContainerHigh = Color(0xFF244049),
    surfaceContainerHighest = Color(0xFF2D4D56),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFFAAB5C7),
    outlineVariant = Color(0xFF3A4960)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C4D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8DF9C9),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4E5FAD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE2FF),
    onSecondaryContainer = Color(0xFF07164F),
    tertiary = Color(0xFF9C422D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBD1),
    onTertiaryContainer = Color(0xFF3B0B02),
    background = TideSaltPaper,
    onBackground = Color(0xFF17201C),
    surface = TideSaltPaper,
    onSurface = Color(0xFF17201C),
    surfaceVariant = Color(0xFFDCE8E2),
    onSurfaceVariant = Color(0xFF3F4A45),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F5F0),
    surfaceContainer = Color(0xFFEAF1EC),
    surfaceContainerHigh = Color(0xFFE3ECE6),
    surfaceContainerHighest = Color(0xFFDCE7E1),
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
