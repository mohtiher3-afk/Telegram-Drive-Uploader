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
    primary = Color(0xFFD8F56A),
    onPrimary = Color(0xFF273400),
    primaryContainer = Color(0xFF506800),
    onPrimaryContainer = Color(0xFFE6FF98),
    secondary = Color(0xFFD7B9FF),
    onSecondary = Color(0xFF3A155C),
    secondaryContainer = Color(0xFF512A73),
    onSecondaryContainer = Color(0xFFF0DBFF),
    tertiary = Color(0xFFFFB0C8),
    onTertiary = Color(0xFF5A1730),
    tertiaryContainer = Color(0xFF7A2948),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF17131D),
    onBackground = Color(0xFFF2EAF5),
    surface = Color(0xFF17131D),
    onSurface = Color(0xFFF2EAF5),
    surfaceVariant = Color(0xFF4B4055),
    onSurfaceVariant = Color(0xFFD6C6DC),
    surfaceContainer = Color(0xFF211A29),
    surfaceContainerHigh = Color(0xFF2B2235),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = Color(0xFF9E8FA5),
    outlineVariant = Color(0xFF4B4055)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF526A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F56A),
    onPrimaryContainer = Color(0xFF172000),
    secondary = Color(0xFF70518E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEDBFF),
    onSecondaryContainer = Color(0xFF28113F),
    tertiary = Color(0xFF9A405D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E071C),
    background = Color(0xFFFFF8FF),
    onBackground = Color(0xFF1E1A21),
    surface = Color(0xFFFFF8FF),
    onSurface = Color(0xFF1E1A21),
    surfaceVariant = Color(0xFFEAE0EE),
    onSurfaceVariant = Color(0xFF4B4350),
    surfaceContainer = Color(0xFFF5ECF7),
    surfaceContainerHigh = Color(0xFFEFE4F2),
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF7C7180),
    outlineVariant = Color(0xFFD0C5D2)
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
