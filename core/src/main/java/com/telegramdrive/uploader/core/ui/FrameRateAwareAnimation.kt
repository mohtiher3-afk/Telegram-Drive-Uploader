package com.telegramdrive.uploader.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.telegramdrive.uploader.core.ui.components.formatRefreshRate
import java.lang.System.currentTimeMillis

/**
 * Provides animation utilities optimized for the device's screen refresh rate.
 *
 * Automatically adjusts animation duration and easing curves based on the
 * detected refresh rate (60Hz, 90Hz, or 120Hz) to ensure smooth, fluid
 * animations on all devices.
 */
object FrameRateAwareAnimation {

    /**
     * Returns the recommended animation duration for the current refresh rate.
     *
     * @param context application context for detecting refresh rate
     * @return animation duration in milliseconds
     */
    @Composable
    fun getAnimationDuration(context: Context = LocalContext.current): Int {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return when {
            refreshRate >= 120 -> 200
            refreshRate >= 90 -> 250
            else -> 300
        }
    }

    /**
     * Returns the recommended easing duration for the current refresh rate.
     *
     * @param context application context for detecting refresh rate
     * @return easing duration in milliseconds
     */
    @Composable
    fun getEasingDuration(context: Context = LocalContext.current): Int {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return when {
            refreshRate >= 120 -> 150
            refreshRate >= 90 -> 200
            else -> 250
        }
    }

    /**
     * Animates a float value with a duration tuned for the current refresh rate.
     *
     * Uses [FastOutSlowInEasing] for natural motion and auto-adjusts duration
     * based on the detected refresh rate.
     *
     * @param targetValue the target value to animate to
     * @return the animated float value
     */
    @Composable
    fun animateRefreshRateAware(
        targetValue: Float,
        animationDuration: Int = getAnimationDuration()
    ): Float {
        val duration = animationDuration
        return animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            )
        ).value
    }

    /**
     * Returns a description string for the current refresh rate.
     *
     * @param context application context for detecting refresh rate
     * @return formatted string like "120Hz", "90Hz", or "60Hz"
     */
    @Composable
    fun getRefreshRateDescription(context: Context = LocalContext.current): String {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return formatRefreshRate(refreshRate)
    }
}
