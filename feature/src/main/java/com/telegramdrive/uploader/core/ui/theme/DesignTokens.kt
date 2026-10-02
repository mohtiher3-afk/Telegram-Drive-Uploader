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

    // Aliases for compatibility during migration
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

object SafeGlowTokens {
    val HeroGlowColor = 0.2f // Alpha
    val AmbientGlowAlpha = 0.15f
    val PulseAlpha = 0.12f
    val SignalAlpha = 0.52f
    val GlowAlpha = 0.30f
}

object LiquidGlassTokens {
    val ReflectionAlphaHigh = 0.055f
    val ReflectionAlphaLow = 0.014f
    val BorderAlpha = 0.15f
    val AccentAlpha = 0.075f
    val RimAlphaHigh = 0.84f
    val RimAlphaMedium = 0.28f
    val RimAlphaLow = 0.36f
}

object AppContentWidth {
    val max: Dp = 1200.dp
}

/**
 * Unified radius scale. Every surface in the app picks one of these so cards,
 * tiles, buttons and sheets read as one family instead of five.
 */
object AppRadius {
    val chip: Dp = 10.dp      // pills, tags, small inline controls
    val control: Dp = 14.dp   // buttons, text fields, nav indicator
    val card: Dp = 20.dp      // default surface
    val feature: Dp = 28.dp   // hero / feature blocks
    val sheet: Dp = 28.dp     // bottom sheets and dialogs
}

/**
 * Elevation is expressed as a hairline + surface step rather than a shadow.
 * Dark UIs read elevation poorly from shadow alone, so the ladder below is the
 * only supported way to make one surface sit above another.
 */
object AppElevation {
    /** Flat surface sitting directly on the background. */
    const val Flat: Int = 0
    /** Raised content surface (cards, tiles). */
    const val Raised: Int = 1
    /** Overlaid chrome (nav bar, top app bar, sheets). */
    const val Chrome: Int = 2

    /** Hairline alpha used on every raised surface. */
    const val HairlineAlpha: Float = 0.10f
}

/**
 * Type roles. Screens must reference these instead of hardcoding `sp` values,
 * which is how the old 38sp screen titles appeared.
 */
object AppType {
    /** Screen title. Deliberately restrained: the title must not out-shout content. */
    val ScreenTitleSize = 26.sp
    /** Section heading above a group of cards. */
    val SectionTitleSize = 17.sp
    /** Emphasised value inside a card (the number in a stat tile). */
    val ValueSize = 26.sp
    /** Hero headline used at most once per screen. */
    val HeroSize = 32.sp
}

/**
 * Unified color palette. All surfaces, text, and accents must reference these
 * instead of hardcoding hex values or relying on MaterialTheme.colorScheme.
 */
object AppColors {
    // Every role Material defines delegates to the dark scheme in Theme.kt.
    // The app is dark-only by product decision (2026-10-02): the home experience
    // is authored against the near-black reference. Pinning to LightColorScheme
    // made onPrimary resolve to white on a pale primaryContainer canvas, which
    // measured 1.30:1 on the physical device.
    // docs/design/DESIGN_SYSTEM.md names the theme's semantic colour scheme as the
    // system of record, so restating hex values here is exactly what let this flat
    // palette drift away from the theme. Delegating makes that drift impossible.
    val primary = DarkColorScheme.primary
    val primaryContainer = DarkColorScheme.primaryContainer
    val secondaryContainer = DarkColorScheme.secondaryContainer
    val surface = DarkColorScheme.surface
    val onPrimary = DarkColorScheme.onPrimary
    val onSecondary = DarkColorScheme.onSecondary
    val onSurface = DarkColorScheme.onSurface
    val onPrimaryContainer = DarkColorScheme.onPrimaryContainer
    val onSecondaryContainer = DarkColorScheme.onSecondaryContainer
    val onSurfaceVariant = DarkColorScheme.onSurfaceVariant
    val error = DarkColorScheme.error
    val errorContainer = DarkColorScheme.errorContainer

    // Container ladder, tonally ordered:
    // surfaceContainerLow < surfaceContainer < surfaceContainerHigh < surfaceContainerHighest.
    // Card and sheet call sites read these steps.
    val surfaceContainerLow = DarkColorScheme.surfaceContainerLow
    val surfaceContainerHigh = DarkColorScheme.surfaceContainerHigh
    val surfaceContainerHighest = DarkColorScheme.surfaceContainerHighest

    // Status and decorative roles have no Material counterpart, so they keep their
    // own values.
    val warning = Color(0xFFFFA726)
    val success = Color(0xFF4CAF50)
    val info = Color(0xFF2196F3)

    // Glow presets (used by liquidGlassOverlay, glowBento, etc.)
    val glowPrimary = Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val glowSecondary = Color(0xFF00E5FF).copy(alpha = 0.12f)
    val glowAccent = Color(0xFF00C853).copy(alpha = 0.20f)
}

/**
 * Unified typography scale. Use AppType.Size tokens for sizing and
 * MaterialTheme.typography for weight/letter-spacing.
 */
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

/**
 * Namespace container for the token groups.
 *
 * Screens read tokens as `DesignTokens.AppColors.*` and `DesignTokens.AppSpacing.*`.
 * The groups themselves are top-level objects in this package, so both access
 * styles resolve: a direct `import ...theme.AppColors` and the grouped
 * `DesignTokens.AppColors`. Without this container every grouped call site fails
 * with "Unresolved reference 'DesignTokens'".
 */
object DesignTokens {
    val AppColors = com.telegramdrive.uploader.core.ui.theme.AppColors
    val AppSpacing = com.telegramdrive.uploader.core.ui.theme.AppSpacing
    val AppRadius = com.telegramdrive.uploader.core.ui.theme.AppRadius
    val AppElevation = com.telegramdrive.uploader.core.ui.theme.AppElevation
    val AppType = com.telegramdrive.uploader.core.ui.theme.AppType
    val AppTypography = com.telegramdrive.uploader.core.ui.theme.AppTypography
    val AppContentWidth = com.telegramdrive.uploader.core.ui.theme.AppContentWidth
    val SafeGlowTokens = com.telegramdrive.uploader.core.ui.theme.SafeGlowTokens
    val LiquidGlassTokens = com.telegramdrive.uploader.core.ui.theme.LiquidGlassTokens

    /** Flat alias retained for call sites that predate the grouped names. */
    val primaryContainer = AppColors.primaryContainer

    /** Flat alias retained for call sites that predate the `AppSpacing` name. */
    val spacingM = AppSpacing.medium
}
