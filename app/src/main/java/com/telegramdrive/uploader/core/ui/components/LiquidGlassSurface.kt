package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * Decorative dark-theme-only reflection layer for bounded M3 surfaces.
 * It does not add semantics, interaction, state, or continuous motion.
 */
@Composable
fun Modifier.liquidGlassOverlay(
    shape: Shape,
    accent: Color = MaterialTheme.colorScheme.primary
): Modifier {
    val darkGlass = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    if (!darkGlass) return this

    return clip(shape)
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.20f),
                    accent.copy(alpha = 0.52f),
                    Color.White.copy(alpha = 0.06f)
                )
            ),
            shape = shape
        )
        .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.055f),
                    Color.White.copy(alpha = 0.014f),
                    Color.Transparent
                ),
                start = Offset(size.width * 0.12f, 0f),
                end = Offset(size.width * 0.82f, size.height * 0.56f)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.075f), Color.Transparent),
                center = Offset(size.width * 0.92f, size.height * 0.92f),
                radius = size.minDimension * 0.70f
            ),
            radius = size.minDimension * 0.70f,
            center = Offset(size.width * 0.92f, size.height * 0.92f)
        )
    }
}

/** A still, semantic rim for existing primary actions and selected controls. */
@Composable
fun Modifier.glowSignalRim(
    shape: Shape,
    accent: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
): Modifier {
    val darkGlass = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    if (!enabled || !darkGlass) return this

    return border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                accent.copy(alpha = 0.84f),
                Color.White.copy(alpha = 0.28f),
                accent.copy(alpha = 0.36f)
            )
        ),
        shape = shape
    )
}
