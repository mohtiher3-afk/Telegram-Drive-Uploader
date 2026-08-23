package com.telegramdrive.uploader.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/** Semantic motion tokens for short, non-blocking Compose state transitions. */
object AppMotion {
    const val fastMillis: Int = 160
    const val shortMillis: Int = 220
    const val mediumMillis: Int = 280

    val standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    fun <T> shortTween(): FiniteAnimationSpec<T> = tween(
        durationMillis = shortMillis,
        easing = standardEasing
    )

    fun offsetShortTween(): FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = shortMillis,
        easing = standardEasing
    )
}
