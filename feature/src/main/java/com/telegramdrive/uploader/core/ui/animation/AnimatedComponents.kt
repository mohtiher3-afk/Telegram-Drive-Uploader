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
import androidx.compose.ui.unit.dp

import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled


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