package com.telegramdrive.uploader.core.ui.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * Centralized animation utilities for beautiful screen transitions and content animations.
 */
object UiAnimations {

    /** Screen enter/exit durations */
    const val screenEnterMillis = 320
    const val screenExitMillis = 240
    const val contentEnterMillis = 280
    const val contentExitMillis = 200

    /** Stagger delay between list items */
    const val itemStaggerMillis = 60

    /**
     * Animated content switch with fade + scale transition.
     * Use for switching between states (e.g., loading/content/error).
     */
    @Composable
    fun <S> AnimatedContentSwitch(
        targetState: S,
        modifier: Modifier = Modifier,
        content: @Composable (S) -> Unit
    ) {
        val motionEnabled = rememberSystemMotionEnabled()

        AnimatedContent(
            targetState = targetState,
            transitionSpec = {
                val enter = fadeIn(animationSpec = tween(contentEnterMillis, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.96f, animationSpec = tween(contentEnterMillis, easing = FastOutSlowInEasing))
                val exit = fadeOut(animationSpec = tween(contentExitMillis, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.98f, animationSpec = tween(contentExitMillis, easing = FastOutSlowInEasing))

                enter togetherWith exit
            },
            modifier = modifier,
            content = { content(it) }
        )
    }

    /**
     * Animated visibility with fade + expand.
     * Use for showing/hiding UI elements conditionally.
     */
    @Composable
    fun AnimatedFadeIn(
        visible: Boolean,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(contentEnterMillis, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(contentExitMillis, easing = FastOutSlowInEasing)),
            modifier = modifier
        ) {
            content()
        }
    }

    /**
     * Staggered entry animation for list items.
     * Returns the delay in milliseconds for the given index.
     */
    fun itemDelay(index: Int): Int = index * itemStaggerMillis

    /**
     * Animated alpha for progress indicators.
     */
    @Composable
    fun rememberProgressAlpha(
        visible: Boolean
    ): Float {
        val motionEnabled = rememberSystemMotionEnabled()
        val targetAlpha = if (visible) 1f else 0f
        val alpha by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(
                durationMillis = if (motionEnabled) contentEnterMillis else 0,
                easing = FastOutSlowInEasing
            ),
            label = "progressAlpha"
        )
        return alpha
    }

    /**
     * Full-screen loading overlay with fade animation.
     */
    @Composable
    fun LoadingOverlay(
        visible: Boolean,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(screenEnterMillis, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(screenExitMillis, easing = FastOutSlowInEasing)),
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}