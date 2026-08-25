package com.telegramdrive.uploader.feature.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

internal data class GlassCardHoverState(
    val modifier: Modifier,
    val borderAlpha: Float
)

/** Pointer-only affordance for display cards; it adds no click or focus semantics. */
@Composable
internal fun rememberGlassCardHover(
    darkGlass: Boolean,
    label: String
): GlassCardHoverState {
    val motionEnabled = rememberSystemMotionEnabled()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val active = darkGlass && motionEnabled && hovered
    val lift by animateDpAsState(
        targetValue = if (active) (-2).dp else 0.dp,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "${label}_lift"
    )
    val scale by animateFloatAsState(
        targetValue = if (active) 1.006f else 1f,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "${label}_scale"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (active) 0.92f else 0.62f,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "${label}_border"
    )
    val modifier = if (darkGlass) {
        Modifier
            .hoverable(interactionSource)
            .graphicsLayer {
                translationY = lift.toPx()
                scaleX = scale
                scaleY = scale
            }
    } else {
        Modifier
    }
    return GlassCardHoverState(modifier, borderAlpha)
}
