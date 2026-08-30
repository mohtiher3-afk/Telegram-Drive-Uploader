package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.TideCoral
import com.telegramdrive.uploader.core.ui.theme.TideHorizon
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

/** Shared dark-field depth and one finite entry transition for each app destination. */
@Composable
fun MissionControlPage(
    pageKey: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val motionEnabled = rememberSystemMotionEnabled()
    val darkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var entered by remember(pageKey) { mutableStateOf(false) }
    LaunchedEffect(pageKey) { entered = true }
    val alpha by animateFloatAsState(
        targetValue = if (entered || !motionEnabled) 1f else 0f,
        animationSpec = AppMotion.pageTween(motionEnabled),
        label = "mission_control_page_alpha"
    )
    val translation by animateIntAsState(
        targetValue = if (entered || !motionEnabled) 0 else 16,
        animationSpec = AppMotion.pageTween(motionEnabled),
        label = "mission_control_page_translation"
    )
    val topGlow = TideCoral.copy(alpha = if (darkSurface) 0.18f else 0f)
    val bottomGlow = TideHorizon.copy(alpha = if (darkSurface) 0.14f else 0f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(topGlow, Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.88f, 0f),
                        radius = size.minDimension * 0.70f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bottomGlow, Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.04f, size.height * 0.88f),
                        radius = size.minDimension * 0.64f
                    )
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = translation.dp.toPx()
                },
            content = content
        )
    }
}
