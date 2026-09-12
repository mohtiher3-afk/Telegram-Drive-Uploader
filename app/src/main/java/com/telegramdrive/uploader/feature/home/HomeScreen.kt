package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.ui.components.GradientCard
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import com.telegramdrive.uploader.domain.model.TelegramConnectionState

/** Calm, near-slate gradient for low-emphasis stat tiles. */
private val CalmSlate = GradientPalette(
    top = Color(0xFF23262E),
    mid = Color(0xFF1E2128),
    base = Color(0xFF181B21),
    glow = Color(0xFF2A2E38)
)

/** Subtle dark gradient tile for the empty active-uploads state. */
private val EmptyTile = GradientPalette(
    top = Color(0xFF20242C),
    mid = Color(0xFF1C1F26),
    base = Color(0xFF181B21),
    glow = Color(0xFF2A2E38),
    content = Color(0xFFB7BDC8)
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onVideosSelected: (List<Uri>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val motionEnabled = rememberSystemMotionEnabled()
    val authorized = uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED
    val connectionAccent by animateColorAsState(
        targetValue = if (authorized) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        },
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "connection_accent"
    )
    val displayName = uiState.telegramUser?.let { user ->
        "${user.firstName} ${user.lastName ?: ""}".trim()
    }.orEmpty()

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) onVideosSelected(uris)
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (displayName.isNotBlank()) {
                            stringResource(R.string.home_greeting, displayName)
                        } else {
                            stringResource(R.string.telegram_drive)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppSpacing.phoneEdge),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
        item {
                    TelegramConnectionCard(
                        telegramState = uiState.telegramConnectionState,
                        telegramUserName = displayName.ifBlank { null },
                        telegramUserHandle = uiState.telegramUser?.username,
                        onTelegramConnectClick = onConnectClick,
                        modifier = Modifier
                            .padding(top = AppSpacing.xs)
                            .testTag("telegram_status_card")
                    )
                }

                item {
                    UploadFeatureCard(
                        onSelectVideos = {
                            pickerLauncher.launch(arrayOf("*/*"))
                        },
                        modifier = Modifier
                            .animateContentSize(animationSpec = AppMotion.shortTween(motionEnabled))
                            .testTag("upload_hero_card")
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.home_upload_snapshot),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        StatCard(
                            title = stringResource(R.string.total_videos),
                            value = uiState.totalVideosCount.toString(),
                            icon = Icons.Default.VideoLibrary,
                            palette = CalmSlate,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_total_videos")
                        )
                        StatCard(
                            title = stringResource(R.string.total_size),
                            value = formatFileSize(uiState.totalSize),
                            icon = Icons.Default.Storage,
                            palette = CalmSlate,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_total_size")
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        StatCard(
                            title = stringResource(R.string.pending),
                            value = uiState.pendingCount.toString(),
                            icon = Icons.Default.Schedule,
                            palette = GradientPalettes.Ocean,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_pending")
                        )
                        StatCard(
                            title = stringResource(R.string.completed),
                            value = uiState.completedCount.toString(),
                            icon = Icons.Default.CheckCircle,
                            palette = GradientPalettes.Mint,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stat_completed")
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.active_uploads),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusPill(
                            activeCount = uiState.activeUploads.size,
                            accent = connectionAccent
                        )
                    }
                }

                if (uiState.activeUploads.isEmpty()) {
                    item {
                        GradientCard(
                            palette = EmptyTile,
                            modifier = Modifier.fillMaxWidth(),
                            glowAlpha = 0.12f
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = null
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.no_active_uploads),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("no_active_uploads_text")
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.activeUploads, key = { "active_${it.id}" }) { upload ->
                        Column(modifier = Modifier.padding(vertical = AppSpacing.xs)) {
                            VideoItem(video = upload)
                            UploadStatusIndicator(
                                video = upload,
                                modifier = Modifier.padding(top = AppSpacing.xs)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(AppSpacing.largeSection)) }
            }
        }
    }
}

@Composable
private fun TelegramConnectionCard(
    telegramState: TelegramConnectionState,
    telegramUserName: String?,
    telegramUserHandle: String?,
    onTelegramConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tgAuthorized = telegramState == TelegramConnectionState.AUTHORIZED
    val palette = if (tgAuthorized) GradientPalettes.Mint else GradientPalettes.Ocean

    GradientCard(
        palette = palette,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = palette.content.copy(alpha = 0.14f),
                contentColor = palette.content
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Telegram",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.content
                )
                Text(
                    text = if (tgAuthorized) {
                        telegramUserName ?: stringResource(R.string.telegram_connected)
                    } else {
                        stringResource(R.string.telegram_not_connected)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.content.copy(alpha = 0.82f)
                )
            }

            if (!tgAuthorized) {
                OutlinedButton(
                    onClick = onTelegramConnectClick,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = palette.content
                    )
                ) {
                    Text(
                        text = stringResource(R.string.connect),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = palette.content,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun UploadFeatureCard(
    onSelectVideos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = GradientPalettes.Neon

    GradientCard(
        palette = palette,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.new_upload),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.content.copy(alpha = 0.82f)
                )
                Text(
                    text = stringResource(R.string.select_files_from_telegram),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = palette.content
                )
            }
            Button(
                onClick = onSelectVideos,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.content,
                    contentColor = palette.base
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(stringResource(R.string.select))
            }
        }
    }
}

@Composable
private fun StatusPill(
    activeCount: Int,
    accent: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = 0.18f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        Text(
            text = activeCount.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    palette: GradientPalette = GradientPalettes.Neon,
    modifier: Modifier = Modifier
) {
    GradientCard(
        palette = palette,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = palette.content.copy(alpha = 0.14f),
                contentColor = palette.content
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = palette.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = palette.content.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
