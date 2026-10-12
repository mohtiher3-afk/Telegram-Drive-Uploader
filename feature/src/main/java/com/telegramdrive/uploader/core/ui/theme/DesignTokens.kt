package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared layout tokens used to keep screen density and large-screen width consistent. */
object AppSpacing {
    val xSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val xs = xSmall
    val sm = small
    val md = medium
    val lg = large
    val xl = extraLarge
    val phoneEdge = medium
    val phoneSection = extraLarge
    val phoneNavInset: Dp = 4.dp
    val largeSection: Dp = 40.dp
    val touchTarget = 48.dp
}

object AppContentWidth {
    val max: Dp = 1200.dp
}

object AppRadius {
    val chip: Dp = 10.dp
    val control: Dp = 14.dp
    val card: Dp = 20.dp
    val feature: Dp = 28.dp
    val sheet: Dp = 28.dp
}

object AppElevation {
    const val Flat: Int = 0
    const val Raised: Int = 1
    const val Chrome: Int = 2
    const val HairlineAlpha: Float = 0.10f
}

object AppType {
    val ScreenTitleSize = 26.sp
    val SectionTitleSize = 17.sp
    val ValueSize = 26.sp
    val HeroSize = 32.sp
}

object AppColors {
    // Dusk palette from tokens.json — used as the fallback when dynamic color
    // is disabled. When dynamic color is enabled, screens should read
    // MaterialTheme.colorScheme directly for the primary roles.
    val primaryContainer = DuskPrimaryContainerDark
    val primary = DuskPrimaryDark
    val secondaryContainer = DuskSurfaceHighDark
    val secondary = DuskOnSurfaceVariantDark
    val surface = DuskSurfaceDark
    val onPrimary = DuskOnPrimaryDark
    val onSecondary = DuskSurfaceDark
    val onSurface = DuskOnSurfaceDark
    val onPrimaryContainer = DuskOnPrimaryContainerDark
    val onSecondaryContainer = DuskOnSurfaceDark
    val onSurfaceVariant = DuskOnSurfaceVariantDark
    val error = DuskErrorDark
    val errorContainer = DuskSurfaceHighDark
    val surfaceContainerLow = DuskSurfaceContainerDark
    val surfaceContainerHigh = DuskSurfaceHighDark
    val surfaceContainerHighest = DuskSurfaceHighDark
    val warning = Color(0xFFFFA726)
    val success = Color(0xFF4CAF50)
    val info = Color(0xFF2196F3)
    val background = Color(0xFF0C0A10)
    val contentPrimary = Color(0xFFFFFFFF)
    val contentMuted = Color(0xFFAFA6BA)
    val lime = Color(0xFFD4A3FF)
    val limeLight = Color(0xFFEBDEFD)
    val onLime = Color(0xFF351551)
    val teal = Color(0xFF2DD4BF)
    val purple = Color(0xFF7C3AED)
    val contentPrimaryInverse = Color(0xFF0C0A10)
    val onPurple = Color(0xFFFFFFFF)
    val purpleHot = Color(0xFFD4A3FF)
    val danger = Color(0xFFEF4444)
    val dangerFill = Color(0xFF2A1518)
    val dangerOnFill = Color(0xFFFF8A8A)
    val dangerSolid = Color(0xFFDC2626)
    val onDangerSolid = Color(0xFFFFFFFF)
    val purpleSoft = Color(0xFFB49AD5)
    val bannerStart = Color(0xFF21192B)
    val bannerEnd = Color(0xFF17151D)
    val bannerBorder = Color(0x1FA775D6)
    val surfaceCard = Color(0xFF18141E)
    val amber = Color(0xFFF59E0B)
    val gridLine = Color(0xFFFFFFFF).copy(alpha = 0.03f)
    val glassFill = Color(0xFFFFFFFF).copy(alpha = 0.055f)
    val glassBorder = Color(0xFFFFFFFF).copy(alpha = 0.10f)
    val glowPrimary = Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val glowSecondary = Color(0xFFD4A3FF).copy(alpha = 0.12f)
    val glowAccent = Color(0xFFD4A3FF).copy(alpha = 0.20f)
}

object AppTypography {
    val headline1 = FontSpec(AppType.HeroSize, FontWeight.Bold)
    val headline2 = FontSpec(AppType.ScreenTitleSize, FontWeight.SemiBold)
    val subtitle1 = FontSpec(AppType.SectionTitleSize, FontWeight.Medium)
    val body1 = FontSpec(16.sp, FontWeight.Normal)
    val body2 = FontSpec(14.sp, FontWeight.Normal)
    val caption = FontSpec(12.sp, FontWeight.Light)
}

data class FontSpec(
    val fontSize: androidx.compose.ui.unit.TextUnit,
    val fontWeight: FontWeight
)

object DesignTokens {
    val AppColors = com.telegramdrive.uploader.core.ui.theme.AppColors
    val AppSpacing = com.telegramdrive.uploader.core.ui.theme.AppSpacing
    val AppRadius = com.telegramdrive.uploader.core.ui.theme.AppRadius
    val AppElevation = com.telegramdrive.uploader.core.ui.theme.AppElevation
    val AppType = com.telegramdrive.uploader.core.ui.theme.AppType
    val AppTypography = com.telegramdrive.uploader.core.ui.theme.AppTypography
    val AppContentWidth = com.telegramdrive.uploader.core.ui.theme.AppContentWidth
    val primaryContainer = AppColors.primaryContainer
    val spacingM = AppSpacing.medium
}
