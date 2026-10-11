package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DesignTokens

/**
 * The fanned stat deck — the design's signature element (docs/maintenance/DESIGN_SPEC.md §5 `.stk`).
 *
 * A front card carries the headline metric (large Doto readout) while two cards
 * behind it, rotated a few degrees and offset upward, reveal supporting metrics.
 * On entry the stack springs up from below with a coordinated fan-in rather than
 * three independent bounces.
 *
 * @param headlineLabel caption for the primary metric
 * @param headlineValue the primary metric (rendered large)
 * @param secondaryLabel caption for the second metric
 * @param secondaryValue the second metric
 * @param tertiaryLabel caption for the third metric
 * @param tertiaryValue the third metric
 * @param accent the readout colour for the front card
 */
@Composable
fun FanDeck(
    headlineLabel: String,
    headlineValue: String,
    secondaryLabel: String,
    secondaryValue: String,
    tertiaryLabel: String,
    tertiaryValue: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    // One entry animation drives the whole stack (choreographed, not per-card).
    val entry by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        label = "fanDeckEntry"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Tertiary card — furthest back, rotated -4deg, raised.
        FanDeckBackCard(
            value = tertiaryValue,
            label = tertiaryLabel,
            accent = DesignTokens.AppColors.purpleHot,
            container = DesignTokens.AppColors.surfaceCard,
            rotation = -4f,
            offsetY = (108).dp,
            offsetX = (-16).dp,
            progress = entry,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        // Secondary card — middle, rotated +3deg.
        FanDeckBackCard(
            value = secondaryValue,
            label = secondaryLabel,
            accent = DesignTokens.AppColors.purpleHot,
            container = DesignTokens.AppColors.surfaceCard,
            rotation = 3f,
            offsetY = (60).dp,
            offsetX = (14).dp,
            progress = entry,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        // Front card — headline metric, large readout, solid accent.
        FanDeckFrontCard(
            value = headlineValue,
            label = headlineLabel,
            accent = accent,
            progress = entry,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun FanDeckBackCard(
    value: String,
    label: String,
    accent: Color,
    container: Color,
    rotation: Float,
    offsetY: Dp,
    offsetX: Dp,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Fan-in: translate up + fade as progress rises.
    val y = (1f - progress) * 40f
    Box(
        modifier = modifier
            .offset(y = offsetY + y.dp, x = offsetX)
            .graphicsLayer {
                rotationZ = rotation
                alpha = progress
            }
            .fillMaxWidth(0.82f)
            .height(150.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(
                Brush.verticalGradient(
                    listOf(container, container.copy(alpha = 0.7f))
                )
            )
            .padding(AppSpacing.medium),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
            Spacer(Modifier.size(AppSpacing.small))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FanDeckFrontCard(
    value: String,
    label: String,
    accent: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val y = (1f - progress) * 56f
    Box(
        modifier = modifier
            .offset(y = y.dp)
            .graphicsLayer { alpha = progress }
            .fillMaxWidth(0.94f)
            .height(160.dp)
            .clip(RoundedCornerShape(AppRadius.card))
            .background(
                Brush.linearGradient(
                    listOf(accent, accent.copy(alpha = 0.85f))
                )
            )
            .padding(AppSpacing.large)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(AppSpacing.small))
            Text(
                text = value,
                // Doto display, large readout — the signature number.
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
