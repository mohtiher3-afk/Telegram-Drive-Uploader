package com.telegramdrive.uploader.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
            OnboardingPage(
                title = "Your files, in your Telegram",
                description = "Prepare videos, choose a Telegram destination, and keep your uploads organized in one private workspace.",
                icon = Icons.Default.CloudUpload,
                accent = MaterialTheme.colorScheme.primaryContainer
            ),
            OnboardingPage(
                title = "Upload on your schedule",
                description = "Queue multiple videos, schedule uploads, and track progress without losing your place.",
                icon = Icons.Default.Schedule,
                accent = MaterialTheme.colorScheme.tertiaryContainer
            ),
            OnboardingPage(
                title = "Private by design",
                description = "The app uses the official TDLib client and requests access only when a feature needs it.",
                icon = Icons.Default.Security,
                accent = MaterialTheme.colorScheme.secondaryContainer
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(stringResource(com.telegramdrive.uploader.R.string.skip))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier.size(176.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = pages[page].accent,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (page == 0) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground_image),
                            contentDescription = stringResource(com.telegramdrive.uploader.R.string.telegram_drive),
                            modifier = Modifier
                                .size(128.dp)
                                .clip(MaterialTheme.shapes.large)
                        )
                    } else {
                        Icon(
                            imageVector = pages[page].icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it / 3 } + fadeOut()
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
                    Box(
                        modifier = Modifier
                            .size(if (index == page) 28.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (page < pages.lastIndex) page++ else finishOnboarding()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                if (page == pages.lastIndex) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(if (page < pages.lastIndex) stringResource(com.telegramdrive.uploader.R.string.continue_action) else stringResource(com.telegramdrive.uploader.R.string.choose_permissions))
            }
        }
    }
}
