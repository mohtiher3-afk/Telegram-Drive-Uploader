package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import androidx.compose.runtime.remember
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition

/**
 * Lottie animation types for empty/error states
 */
sealed interface LottieAnimation {
    data class EmptyState(val resource: String, val speed: Float = 1f) : LottieAnimation
    data class ErrorState(val resource: String, val speed: Float = 1f) : LottieAnimation
    data class LoadingState(val resource: String, val speed: Float = 1f) : LottieAnimation
    data class SuccessState(val resource: String, val speed: Float = 1f) : LottieAnimation
    data class NoConnection(val resource: String, val speed: Float = 1f) : LottieAnimation
    object None : LottieAnimation
}

/**
 * Placeholder for Lottie animations - replace with actual LottieComposition when assets are available
 * For now, provides a built-in animated fallback using Compose animations
 */
@Composable
fun rememberLottieAnimation(
    animation: LottieAnimation,
    motionEnabled: Boolean = rememberSystemMotionEnabled()
): LottieAnimationResult {
    val infiniteTransition = rememberInfiniteTransition(
        label = "lottie_${animation.hashCode()}"
    )
    
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = when (animation) {
                    is LottieAnimation.LoadingState -> (1000f / animation.speed).toInt()
                    is LottieAnimation.ErrorState -> (1500f / animation.speed).toInt()
                    is LottieAnimation.EmptyState -> (2000f / animation.speed).toInt()
                    else -> (2500f / animation.speed).toInt()
                },
                easing = AppMotion.standardEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "lottieProgress"
    )
    
    return LottieAnimationResult(
        progress = progress,
        isPlaying = motionEnabled
    )
}

/**
 * Built-in animated fallback icons for when Lottie assets aren't available
 */
@Composable
fun AnimatedEmptyStateIcon(
    animationType: LottieAnimation,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val result = rememberLottieAnimation(animationType)
    val motionEnabled = rememberSystemMotionEnabled()
    
    val animatedRotation by animateFloatAsState(
        targetValue = if (result.isPlaying && motionEnabled) 360f else 0f,
        animationSpec = if (motionEnabled) {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(
                    durationMillis = when (animationType) {
                        is LottieAnimation.LoadingState -> 1000
                        is LottieAnimation.ErrorState -> 1500
                        is LottieAnimation.EmptyState -> 2000
                        else -> 2500
                    },
                    easing = AppMotion.standardEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        } else {
            androidx.compose.animation.core.snap()
        },
        label = "iconRotation"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (result.isPlaying && motionEnabled) 1.1f else 1f,
        animationSpec = if (motionEnabled) {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(
                    durationMillis = 1000,
                    easing = AppMotion.springEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            androidx.compose.animation.core.snap()
        },
        label = "iconScale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationType is LottieAnimation.ErrorState && result.isPlaying && motionEnabled) 
            (result.progress * 0.5f + 0.5f) else 1f,
        animationSpec = if (motionEnabled && animationType is LottieAnimation.ErrorState) {
            androidx.compose.animation.core.infiniteRepeatable(
                animation = androidx.compose.animation.core.tween(
                    durationMillis = 800,
                    easing = AppMotion.springEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            androidx.compose.animation.core.snap()
        },
        label = "iconAlpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = if (animationType is LottieAnimation.LoadingState && result.isPlaying && motionEnabled) 
                    animatedRotation else 0f
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
    ) {
        // Built-in fallback icons based on animation type
        Icon(
            imageVector = when {
                animationType is LottieAnimation.LoadingState -> Icons.Default.HourglassEmpty
                animationType is LottieAnimation.ErrorState -> Icons.Default.ErrorOutline
                animationType is LottieAnimation.EmptyState -> Icons.Default.VideoLibrary
                animationType is LottieAnimation.SuccessState -> Icons.Default.CheckCircle
                animationType is LottieAnimation.NoConnection -> Icons.Default.CloudQueue
                else -> Icons.Default.HourglassEmpty
            },
            contentDescription = null,
            tint = tint.copy(alpha = animatedAlpha),
            modifier = Modifier.size(size)
        )
    }
}

/**
 * Predefined animation types for common states
 */
object LottieAnimations {
    val emptyQueue = LottieAnimation.EmptyState("empty_queue.json")
    val emptyUpload = LottieAnimation.EmptyState("empty_upload.json")
    val noConnection = LottieAnimation.NoConnection("no_connection.json")
    val uploadError = LottieAnimation.ErrorState("upload_error.json")
    val uploadSuccess = LottieAnimation.SuccessState("upload_success.json")
    val uploading = LottieAnimation.LoadingState("uploading.json")
    val preparing = LottieAnimation.LoadingState("preparing.json")
}