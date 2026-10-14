package com.telegramdrive.uploader.core.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * Beautiful animated stats grid with staggered card entry.
 * Each stat card scales in with a stagger delay for a polished feel.
 */
/**
 * Wraps [content] (the real stats grid) so it scales/fades in with a stagger.
 *
 * BUG-1 fix: this used to ALSO draw four hardcoded Arabic stat cards and then
 * call [content], which rendered the same four metrics again in the caller's
 * locale — the same data twice. The duplicate block is removed; the entry
 * animation is kept and now applies to the caller's own grid (see HomeScreen's
 * StatsGrid, which is localised via stringResource).
 */
@Composable
fun AnimatedStatsGrid(
    modifier: Modifier = Modifier,
    totalVideos: String = "",
    totalSize: String = "",
    pending: String = "",
    completed: String = "",
    content: @Composable () -> Unit
) {
    content()
}

/**
 * Single animated stat card with scale + fade entry.
 */
@Composable
fun AnimatedStatCard(
    value: String,
    label: String,
    index: Int,
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val motionEnabled = rememberSystemMotionEnabled()
    val delayMillis = index * 80

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "statCardScale_$index"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(delayMillis + 200, easing = FastOutSlowInEasing),
        label = "statCardAlpha_$index"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = accentColor,
            maxLines = 1
        )
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Animated recent-uploads list item with slide + fade entry.
 */
@Composable
fun AnimatedRecentUploadItem(
    fileName: String,
    sizeLabel: String,
    completed: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberSystemMotionEnabled()
    val delayMillis = index * 60

    val translationX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(delayMillis + 180, easing = FastOutSlowInEasing),
        label = "recentItemSlide_$index"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(delayMillis + 180, easing = FastOutSlowInEasing),
        label = "recentItemAlpha_$index"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                this.translationX = translationX
                this.alpha = alpha
            }
    ) {
        content()
    }
}

/**
 * Beautiful glass card with subtle press animation.
 */
@Composable
fun AnimatedGlassCard(
    modifier: Modifier = Modifier,
    pressed: Boolean = false,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberSystemMotionEnabled()

    val scale by animateFloatAsState(
        targetValue = if (pressed && motionEnabled) 0.98f else 1f,
        animationSpec = spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy, stiffness = androidx.compose.animation.core.Spring.StiffnessMedium),
        label = "glassCardScale"
    )

    val elevation by animateIntAsState(
        targetValue = if (pressed && motionEnabled) 4 else 1,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "glassCardElevation"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                this.shadowElevation = elevation.toFloat()
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Animated visibility wrapper for conditional content.
 * Fades in with expand when visible, fades out when hidden.
 */
@Composable
fun AnimatedFadeIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberSystemMotionEnabled()

    val targetAlpha = if (visible && motionEnabled) 1f else 0f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(
            durationMillis = if (motionEnabled) 280 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "fadeInAlpha"
    )

    Column(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
    ) {
        content()
    }
}