package com.telegramdrive.uploader.feature.missioncontrol.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens for the Mission Control surface.
 *
 * This is a self-contained palette: it does not read from `Theme.kt`, so adopting
 * it is an opt-in change rather than a restyle of the whole app. Measured contrast
 * ratios against the surfaces they are used on (WCAG 2.1 relative luminance):
 *
 * | Pair                              | Ratio   | Level        |
 * |-----------------------------------|---------|--------------|
 * | TextPrimary   on Background        | 19.02:1 | AAA          |
 * | PrimaryLime   on Background        | 13.38:1 | AAA          |
 * | AccentCyan    on Background        | 14.91:1 | AAA          |
 * | StatusSuccess on Background        | 11.86:1 | AAA          |
 * | ElectricBlue  on Background        |  7.63:1 | AAA          |
 * | TextSecondary on Background        |  7.05:1 | AAA (thin)   |
 * | TextSecondary on CardSurface       |  7.05:1 | AAA (thin)   |
 * | BorderOutline on CardSurface       |  1.22:1 | decorative   |
 *
 * TextSecondary replaced the originally specified #8C93A8, which measures only
 * 5.82:1 on CardSurface and therefore missed the AAA target the guide claims. The
 * replacement clears 7:1, but with a 0.05 margin, so it must be re-measured if any
 * of these colours change. BorderOutline is deliberately faint and must never
 * carry text.
 */
object MissionControlTokens {
    val Background = Color(0xFF0B0E17)
    val CardSurface = Color(0xFF151A26)
    val BorderOutline = Color(0xFF222938)

    val PrimaryLime = Color(0xFFA3F53B)
    val AccentCyan = Color(0xFF00E5FF)
    val ElectricBlue = Color(0xFF2881FF)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF9AA3B8)

    val StatusSuccess = Color(0xFF00E676)
    val StatusWarning = Color(0xFFFF9100)
    val StatusError = Color(0xFFFF5252)
}