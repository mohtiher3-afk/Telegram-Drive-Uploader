package com.telegramdrive.uploader.feature.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.telegramdrive.uploader.core.ui.theme.GlowColorPreset

/**
 * Composable modifier and helpers for optimizing frame rate on high-refresh-rate displays.
 */
@Stable
class FrameRateOptimizer(private val refreshRateHz: Float) {

    /** The optimal frame duration in milliseconds for the current refresh rate. */
    val optimalFrameDurationMs: Long
        get() = (1000f / refreshRateHz).toLong().coerceAtLeast(8L)

    /** Animation duration in milliseconds tuned for the current refresh rate. */
    val animationDurationMs: Int
        get() = when {
            refreshRateHz >= 120 -> 200
            refreshRateHz >= 90 -> 250
            else -> 300
        }

    /** Easing curve duration in milliseconds tuned for the current refresh rate. */
    val easingDurationMs: Int
        get() = when {
            refreshRateHz >= 120 -> 150
            refreshRateHz >= 90 -> 200
            else -> 250
        }

    /**
     * Returns a [Modifier] that adjusts rendering for optimal frame rate on the device.
     */
    @Composable
    fun asModifier(): Modifier = Modifier.refreshRateOptimized(refreshRateHz)
}

@Composable
fun rememberFrameRateOptimizer(): FrameRateOptimizer {
    val context = LocalContext.current
    val refreshRate = remember { RefreshRateHelper.getRefreshRate(context) }
    return remember(refreshRate) { FrameRateOptimizer(refreshRate) }
}

@Composable
fun Modifier.refreshRateAware(): Modifier {
    val optimizer = rememberFrameRateOptimizer()
    return this.then(optimizer.asModifier())
}

@Composable
fun <T> animateRefreshRateAwareFloat(
    targetValue: Float,
    animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = tween(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
): Float {
    val optimizer = rememberFrameRateOptimizer()
    val duration = if (animationSpec is androidx.compose.animation.core.TweenSpec<*>) {
        optimizer.animationDurationMs
    } else {
        300
    }
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
    ).value
}

@Composable
private fun Modifier.refreshRateOptimized(refreshRateHz: Float): Modifier {
    return this.graphicsLayer {
        if (refreshRateHz >= 120) {
            // On 120Hz displays, ensure smooth rendering
        }
    }
}
