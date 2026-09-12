package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic gradient stop triple: vivid top melting into a near-black base.
 * Follows the inspiration language (dark bento tiles with glowing tops).
 * Dark-theme only by design decision; light variants derive later.
 */
data class GradientPalette(
    val top: Color,
    val mid: Color,
    val base: Color,
    /** Primary glow color used for rims and ambient highlights. */
    val glow: Color,
    /** Text/icon color guaranteed readable over [base] and [mid]. */
    val content: Color = Color.White
) {
    val stops: List<Color> get() = listOf(top, mid, base)
}

/** Gradient bento palettes sampled from the approved dark inspiration set. */
object GradientPalettes {
    /** Sunset ember: coral-orange tile (LED-strip / error / warning energy). */
    val Sunset = GradientPalette(
        top = Color(0xFFF2814F),
        mid = Color(0xFFB4502E),
        base = Color(0xFF1A1210),
        glow = Color(0xFFF2814F)
    )

    /** Deep ocean: saturated blue tile (info / paused / destination energy). */
    val Ocean = GradientPalette(
        top = Color(0xFF4D7CFE),
        mid = Color(0xFF2B3FB8),
        base = Color(0xFF0D1330),
        glow = Color(0xFF4D7CFE)
    )

    /** Neon violet: electric purple tile (active / primary action energy). */
    val Neon = GradientPalette(
        top = Color(0xFF9B5CF6),
        mid = Color(0xFF5B34B8),
        base = Color(0xFF150E33),
        glow = Color(0xFF9B5CF6)
    )

    /** Mint signal: teal-green tile (success / completed energy). */
    val Mint = GradientPalette(
        top = Color(0xFF57C7A3),
        mid = Color(0xFF2E8B74),
        base = Color(0xFF0C211C),
        glow = Color(0xFF57C7A3)
    )

    /**
     * Semantic binding for upload lifecycle states. Green-family (Mint) is
     * reserved for confirmed completion, matching [UploadCompletedGreen].
     */
    fun forUploadState(
        isError: Boolean = false,
        isPaused: Boolean = false,
        isCompleted: Boolean = false
    ): GradientPalette = when {
        isError -> Sunset
        isPaused -> Ocean
        isCompleted -> Mint
        else -> Neon
    }
}
