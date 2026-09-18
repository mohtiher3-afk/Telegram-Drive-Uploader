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

import com.telegramdrive.uploader.core.ui.theme.LiquidGlassTokens

/** Visual intensity for bounded glass surfaces; it never carries product state. */
enum class LiquidGlassEmphasis(
    val borderAlpha: Float,
    val reflectionAlpha: Float,
    val ambientAlpha: Float
) {
    Operational(borderAlpha = 0.34f, reflectionAlpha = 0.032f, ambientAlpha = 0.045f),
    FeatureLens(borderAlpha = 0.66f, reflectionAlpha = 0.075f, ambientAlpha = 0.105f)
}

/**
 * Deprecated decorative layer. Kept as a no-op so existing call sites
 * compile unchanged; the Calm Material design removes glass reflections.
 */
@Composable
fun Modifier.liquidGlassReflection(
    shape: Shape,
    accent: Color = MaterialTheme.colorScheme.primary
): Modifier = this

/**
 * Deprecated decorative layer. Kept as a no-op so existing call sites
 * compile unchanged; the Calm Material design removes glass overlays.
 */
@Composable
fun Modifier.liquidGlassOverlay(
    shape: Shape,
    accent: Color = MaterialTheme.colorScheme.primary,
    emphasis: LiquidGlassEmphasis = LiquidGlassEmphasis.Operational
): Modifier = this

/**
 * Deprecated decorative layer. Kept as a no-op so existing call sites
 * compile unchanged; the Calm Material design removes glow rims.
 */
@Composable
fun Modifier.glowSignalRim(
    shape: Shape,
    accent: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
): Modifier = this
