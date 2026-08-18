package com.telegramdrive.uploader.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD3E4FF),
    onPrimary = Color(0xFF001D36),
    primaryContainer = Color(0xFF00448E),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFF97F0FF),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color(0xFFC4C6D0),
    tertiary = Color(0xFF69FFA0),
    onTertiary = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005327),
    onTertiaryContainer = Color(0xFF69FFA0),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkCardBg,
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF8C0012),
    onErrorContainer = Color(0xFFFFDAD8),
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF333842)
)

private val LightColorScheme = lightColorScheme(
    primary = BentoPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = BentoHeroContainer,
    onPrimaryContainer = BentoHeroText,
    secondary = Color(0xFF44474E),
    onSecondary = Color.White,
    secondaryContainer = BentoTileBg,
    onSecondaryContainer = BentoTextPrimary,
    tertiary = BentoPrimaryBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8FCD0),
    onTertiaryContainer = Color(0xFF00210C),
    background = BentoBg,
    onBackground = BentoTextPrimary,
    surface = BentoCardBg,
    onSurface = BentoTextPrimary,
    surfaceVariant = BentoTileBg,
    onSurfaceVariant = BentoTextSecondary,
    surfaceContainer = BentoTileBg,
    surfaceContainerHigh = BentoCardBg,
    error = UploadErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = BentoCardBorder,
    outlineVariant = Color(0xFFC4C6D0)
)

@Composable
fun TelegramDriveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
        content = content
    )
}

