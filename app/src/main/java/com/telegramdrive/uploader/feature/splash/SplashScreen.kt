package com.telegramdrive.uploader.feature.splash

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f
    val pulse = if (reducedMotion) {
        1f
    } else {
        val animatedPulse by rememberInfiniteTransition(label = "splash-pulse").animateFloat(
            initialValue = 0.92f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ),
            label = "splash-pulse-scale"
        )
        animatedPulse
    }

    LaunchedEffect(reducedMotion) {
        delay(if (reducedMotion) 350L else 950L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.30f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = androidx.compose.ui.geometry.Offset(
                        x = 0.5f, y = 0.38f
                    ),
                    radius = 1.2f
                )
            )
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.mission_control_logo),
                contentDescription = stringResource(R.string.splash_logo_description),
                modifier = Modifier
                    .size(132.dp)
                    .graphicsLayer {
                        val scale = if (reducedMotion) 1f else pulse
                        scaleX = scale
                        scaleY = scale
                        rotationZ = if (reducedMotion) 0f else (pulse - 1f) * 18f
                    }
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.splash_starting),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(if (reducedMotion) 1f else 0.86f)
            )
        }
    }
}
