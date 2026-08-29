package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
