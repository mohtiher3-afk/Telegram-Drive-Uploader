package com.telegramdrive.uploader.core.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * Beautiful animated stats grid with staggered card entry.
 * Each stat card scales in with a stagger delay for a polished feel.
 */
@Composable
fun AnimatedStatsGrid(
    modifier: Modifier = Modifier,
    totalVideos: String,
    totalSize: String,
    pending: String,
    completed: String,
    content: @Composable () -> Unit
) {
    val motionEnabled = rememberSystemMotionEnabled()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
    ) {
        // Top row: Total + Size
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)) {
            AnimatedStatCard(
                value = totalVideos,
                label = "مقطع",
                index = 0,
                modifier = Modifier.weight(1f)
            )
            AnimatedStatCard(
                value = totalSize,
                label = "الحجم",
                index = 1,
                modifier = Modifier.weight(1f)
            )
        }
        // Bottom row: Pending + Completed
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)) {
            AnimatedStatCard(
                value = pending,
                label = "قيد الانتظار",
                index = 2,
                modifier = Modifier.weight(1f)
            )
            AnimatedStatCard(
                value = completed,
                label = "مكتمل",
                index = 3,
                modifier = Modifier.weight(1f),
                accentColor = DesignTokens.AppColors.lime
            )
        }
        content()
    }
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
    accentColor: androidx.compose.ui.graphics.Color = DesignTokens.AppColors.purpleHot
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
            .clip(RoundedCornerShape(DesignTokens.AppRadius.card))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DesignTokens.AppColors.surfaceCard,
                        DesignTokens.AppColors.surface.copy(alpha = 0.6f)
                    )
                )
            )
            .padding(DesignTokens.AppSpacing.medium),
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
            color = DesignTokens.AppColors.contentMuted
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
            .clip(RoundedCornerShape(DesignTokens.AppRadius.card))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DesignTokens.AppColors.surfaceCard,
                        DesignTokens.AppColors.surface.copy(alpha = 0.5f)
                    )
                )
            )
            .padding(DesignTokens.AppSpacing.medium)
    ) {
        content()
    }
}

/**
 * Animated progress indicator with smooth color transition.
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = DesignTokens.AppColors.lime
) {
    val motionEnabled = rememberSystemMotionEnabled()

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = if (motionEnabled) 600 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "progressIndicator"
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = DesignTokens.AppColors.surfaceCard.copy(alpha = 0.4f),
    )
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