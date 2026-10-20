package com.telegramdrive.uploader.feature.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.feature.R
import com.telegramdrive.uploader.core.ui.components.DotProgressBar
import com.telegramdrive.uploader.core.ui.components.DotProgressRing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.delay

/**
 * Splash: the mission-control dot ring from the design spec (DESIGN_SPEC §5) lights up
 * clockwise as the app initialises, above a Doto wordmark, then hands off.
 *
 * Layout per the prototype (drive-uploader-ui.html §splash):
 *  1. The mission-control ring (32 dots + 3×5 grid) — fills clockwise
 *  2. "Telegram Drive Uploader" Doto title
 *  3. "(01) Big files. One stream." eyebrow
 *  4. "Initializing relay" + 20-dot loader bar
 *
 * Replaces the old static PNG logo. The ring doubles as the loading indicator,
 * so there is no separate spinner. Reduced-motion users skip the ring animation
 * (it renders full) and get a shorter delay.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f

    // Ring fills 0 -> 1 over the splash window (or jumps to full under reduced motion).
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val ringProgress by animateFloatAsState(
        targetValue = if (started && !reducedMotion) 1f else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 650),
        label = "splashRing"
    )

    // Dot loader bar for "Initializing relay" — 20 dots
    var dotsStarted by remember { mutableStateOf(false) }
    LaunchedEffect(started) {
        if (started) {
            delay(if (reducedMotion) 100L else 400L)
            dotsStarted = true
        }
    }
    val dotsProgress by animateFloatAsState(
        targetValue = if (dotsStarted && !reducedMotion) 1f else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else 1200),
        label = "splashDots"
    )

    LaunchedEffect(reducedMotion) {
        delay(if (reducedMotion) 500L else 1400L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            DotProgressRing(
                progress = ringProgress,
                size = 180.dp,
                contentDescriptionText = stringResource(R.string.splash_starting)
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = DotoFamily,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            // Eyebrow: (01) Big files. One stream.
            Text(
                text = stringResource(R.string.splash_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = DotoFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            // Initializing relay + 20-dot loader
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.initializing_relay),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SpaceMonoFamily,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DotProgressBar(
                    progress = dotsProgress,
                    contentDescriptionText = stringResource(R.string.splash_loading_accessibility),
                    modifier = Modifier.size(200.dp, 8.dp)
                )
            }
        }
    }
}