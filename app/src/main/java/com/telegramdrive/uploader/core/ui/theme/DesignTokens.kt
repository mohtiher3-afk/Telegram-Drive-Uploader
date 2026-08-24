package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared layout tokens used to keep screen density and large-screen width consistent. */
object AppSpacing {
    // Descriptive aliases retained from the original design-token contract.
    val compact = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp

    // Short aliases retained for existing screen call sites.
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val section: Dp = 32.dp
    val largeSection: Dp = 40.dp
    val touchTarget: Dp = 48.dp
}

object AppContentWidth {
    val max: Dp = 1200.dp
}
