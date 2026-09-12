package com.telegramdrive.uploader.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.ui.components.GradientButton
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: GradientPalette
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableIntStateOf(0) }
    val motionEnabled = rememberSystemMotionEnabled()
    val pages = listOf(
            OnboardingPage(
                title = stringResource(R.string.onboarding_page_upload_title),
                description = stringResource(R.string.onboarding_page_upload_description),
                icon = Icons.Default.CloudUpload,
                accent = GradientPalettes.Neon
            ),
            OnboardingPage(
                title = stringResource(R.string.onboarding_page_schedule_title),
                description = stringResource(R.string.onboarding_page_schedule_description),
                icon = Icons.Default.Schedule,
                accent = GradientPalettes.Ocean
            ),
            OnboardingPage(
                title = stringResource(R.string.onboarding_page_private_title),
                description = stringResource(R.string.onboarding_page_private_description),
                icon = Icons.Default.Security,
                accent = GradientPalettes.Neon
            )
        )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.complete()
        onFinished()
    }

    fun finishOnboarding() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.READ_MEDIA_VIDEO)
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)
            else if (Build.VERSION.SDK_INT <= 32 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissions.isEmpty()) {
            viewModel.complete()
            onFinished()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0F12), Color(0xFF15171C))
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GradientPalettes.Neon.glow.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset.Zero
                )
            ),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.complete()
                        onFinished()
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Text(stringResource(com.telegramdrive.uploader.R.string.skip))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val accent = pages[page].accent
            val tileShape = MaterialTheme.shapes.extraLarge
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .clip(tileShape)
                    .background(Brush.verticalGradient(accent.stops))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.glow.copy(alpha = 0.28f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.12f), tileShape),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides accent.content) {
                    if (page == 0) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.mission_control_logo),
                            contentDescription = stringResource(com.telegramdrive.uploader.R.string.telegram_drive),
                            modifier = Modifier
                                .size(128.dp)
                                .clip(MaterialTheme.shapes.large)
                        )
                    } else {
                        Icon(
                            imageVector = pages[page].icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = accent.content
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (slideInHorizontally(animationSpec = AppMotion.shortSpatialSpring(motionEnabled)) { it / 3 } +
                        fadeIn(animationSpec = AppMotion.shortTween<Float>(motionEnabled))).togetherWith(
                        slideOutHorizontally(animationSpec = AppMotion.shortSpatialSpring(motionEnabled)) { -it / 3 } +
                        fadeOut(animationSpec = AppMotion.shortTween<Float>(motionEnabled))
                    ).using(SizeTransform(clip = false))
                },
                label = "onboarding_page"
            ) { targetPage ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pages[targetPage].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pages[targetPage].description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    val active = index == page
                    val dotWidth by animateDpAsState(
                        targetValue = if (active) 28.dp else 8.dp,
                        animationSpec = AppMotion.shortTween(motionEnabled),
                        label = "onboarding_dot_width_$index"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (active) GradientPalettes.Neon.top else MaterialTheme.colorScheme.outlineVariant,
                        animationSpec = AppMotion.shortTween(motionEnabled),
                        label = "onboarding_dot_color_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(dotWidth, 8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            GradientButton(
                text = if (page < pages.lastIndex) {
                    stringResource(com.telegramdrive.uploader.R.string.continue_action)
                } else {
                    stringResource(com.telegramdrive.uploader.R.string.choose_permissions)
                },
                onClick = {
                    if (page < pages.lastIndex) page++ else finishOnboarding()
                },
                palette = GradientPalettes.Neon
            )
        }
    }
}
