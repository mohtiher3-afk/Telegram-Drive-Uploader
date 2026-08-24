package com.telegramdrive.uploader.core.ui.theme

import android.animation.ValueAnimator
import android.os.Build

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset

/** Semantic motion tokens for short, non-blocking Compose state transitions. */
object AppMotion {
    const val fastMillis: Int = 160
    const val shortMillis: Int = 220
    const val mediumMillis: Int = 280

    val standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> shortTween(motionEnabled: Boolean = true): FiniteAnimationSpec<T> = if (motionEnabled) {
        tween(
            durationMillis = shortMillis,
            easing = standardEasing
        )
    } else {
        snap()
    }

    /**
     * Spatial motion for the onboarding page transition. A no-bounce spring keeps
     * the transition interruptible without adding celebration to a utility flow.
     */
    fun shortSpatialSpring(motionEnabled: Boolean = true): FiniteAnimationSpec<IntOffset> = if (motionEnabled) {
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    } else {
        snap()
    }
}

/** Reads Android's animator-duration accessibility setting once per composition. */
@Composable
fun rememberSystemMotionEnabled(): Boolean = remember {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ValueAnimator.areAnimatorsEnabled()
    } else {
        true
    }
}
