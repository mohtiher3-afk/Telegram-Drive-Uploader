package com.telegramdrive.uploader.feature.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.telegramdrive.uploader.core.ui.theme.GlowColorPreset

/**
 * Provides animation utilities optimized for the device's screen refresh rate.
 */
object FrameRateAwareAnimation {

    @Composable
    fun getAnimationDuration(context: android.content.Context = LocalContext.current): Int {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return when {
            refreshRate >= 120 -> 200
            refreshRate >= 90 -> 250
            else -> 300
        }
    }

    @Composable
    fun getEasingDuration(context: android.content.Context = LocalContext.current): Int {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return when {
            refreshRate >= 120 -> 150
            refreshRate >= 90 -> 200
            else -> 250
        }
    }

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

    @Composable
    fun getRefreshRateDescription(context: android.content.Context = LocalContext.current): String {
        val refreshRate = RefreshRateHelper.getRefreshRate(context)
        return formatRefreshRate(refreshRate)
    }
}

fun formatRefreshRate(refreshRate: Float): String {
    return when {
        refreshRate >= 120 -> "120Hz"
        refreshRate >= 90 -> "90Hz"
        refreshRate >= 75 -> "75Hz"
        else -> "60Hz"
    }
}
