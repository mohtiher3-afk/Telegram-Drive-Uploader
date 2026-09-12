package com.telegramdrive.uploader.feature.splash

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val reducedMotion = android.provider.Settings.Global.getFloat(
        context.contentResolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f

    LaunchedEffect(reducedMotion) {
        delay(if (reducedMotion) 350L else 700L)
        onFinished()
    }

    val neon = GradientPalettes.Neon

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0F12), Color(0xFF15171C))
                )
            )
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center
    ) {
        // Faint neon ambience pulled toward the top-left corner for depth.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            neon.glow.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset.Zero
                    )
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Soft glow halo behind the logo (decorative, unmodified artwork).
                Box(
                    modifier = Modifier
                        .size(208.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    neon.glow.copy(alpha = 0.42f),
                                    neon.top.copy(alpha = 0.16f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(R.drawable.mission_control_logo),
                    contentDescription = stringResource(R.string.splash_logo_description),
                    modifier = Modifier.size(120.dp)
                )
            }
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = neon.content
            )
            Text(
                text = stringResource(R.string.splash_starting),
                style = MaterialTheme.typography.labelLarge,
                color = neon.top,
                modifier = Modifier.alpha(0.86f)
            )
        }
    }
}
