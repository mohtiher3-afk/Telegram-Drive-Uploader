package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/**
 * Mission Control Glow focus treatment: a persistent 3dp outline with reserved outer space.
 * It uses the semantic outline role rather than a user-selected Glow color so focus stays visible
 * in light and dark schemes. The actual action and semantics remain owned by the wrapped control.
 */
@Composable
fun Modifier.glowFocusIndicator(
    focused: Boolean,
    shape: Shape = MaterialTheme.shapes.medium
): Modifier {
    val motionEnabled = rememberSystemMotionEnabled()
    val focusColor by animateColorAsState(
        targetValue = if (focused) MaterialTheme.colorScheme.outline else Color.Transparent,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "glowFocusIndicatorColor"
    )
    return this
        .padding(2.dp)
        .border(
            width = 3.dp,
            color = focusColor,
            shape = shape
        )
}
