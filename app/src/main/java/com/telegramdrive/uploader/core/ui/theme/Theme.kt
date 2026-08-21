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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF561E1B),
    primaryContainer = Color(0xFF73332E),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB7),
    onSecondary = Color(0xFF442926),
    secondaryContainer = Color(0xFF5D403C),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFD5C58D),
    onTertiary = Color(0xFF393005),
    tertiaryContainer = Color(0xFF514619),
    onTertiaryContainer = Color(0xFFF2E3A7),
    background = Color(0xFF201110),
    onBackground = Color(0xFFF1DFDD),
    surface = Color(0xFF201110),
    onSurface = Color(0xFFF1DFDD),
    surfaceVariant = Color(0xFF514341),
    onSurfaceVariant = Color(0xFFD8C2BF),
    surfaceContainer = Color(0xFF2D1B1A),
    surfaceContainerHigh = Color(0xFF392523),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFFA98F8C),
    outlineVariant = Color(0xFF514341)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF9C423A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF3B0907),
    secondary = Color(0xFF765652),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF2C1513),
    tertiary = Color(0xFF705E1A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF7E6A6),
    onTertiaryContainer = Color(0xFF241A00),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF241A19),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF241A19),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF5C4140),
    surfaceContainer = Color(0xFFFCE9E6),
    surfaceContainerHigh = Color(0xFFF7E0DD),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF8F716E),
    outlineVariant = Color(0xFFD8C2BF)
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
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
