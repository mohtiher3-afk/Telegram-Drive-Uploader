package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.feature.R

/**
 * Display face: Doto — a dotted/segmented readout font. Used for big numbers,
 * percentages, counts and the app wordmark. Its segmented look is the design's
 * signature; it makes metrics read like an instrument panel rather than body copy.
 * (Per docs/maintenance/DESIGN_SPEC.md.)
 */
val DotoFamily = FontFamily(
    Font(R.font.doto_regular, FontWeight.Normal),
    Font(R.font.doto_bold, FontWeight.Bold),
    Font(R.font.doto_black, FontWeight.Black)
)

/**
 * UI/body face: Space Mono. All non-display text (labels, body, buttons, metadata).
 * Monospace reinforces the "mission control" terminal aesthetic.
 */
val SpaceMonoFamily = FontFamily(
    Font(R.font.space_mono_regular, FontWeight.Normal),
    Font(R.font.space_mono_bold, FontWeight.Bold)
)

/**
 * Shared Material 3 type scale for a utility app: prominent headings support
 * onboarding and screen hierarchy while body and label roles remain readable
 * for file metadata, upload state, and diagnostics.
 */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * The text style every Button/label in the app must use.
 *
 * Material 3's `Button` renders its label with its own internal default style,
 * NOT `labelLarge`, so button text silently falls back to Roboto even when
 * `Typography` is fully remapped. Centralising the style here and applying it
 * explicitly to button text guarantees the Space Mono "mission control" font
 * everywhere. (Per docs/maintenance/DESIGN_SPEC.md.)
 */
val AppButtonTextStyle = TextStyle(
    fontFamily = SpaceMonoFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.6.sp
)

/** Display style for big readout numbers (Doto). */
val AppDisplayTextStyle = TextStyle(
    fontFamily = DotoFamily,
    fontWeight = FontWeight.Black,
    fontSize = 32.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.sp
)
