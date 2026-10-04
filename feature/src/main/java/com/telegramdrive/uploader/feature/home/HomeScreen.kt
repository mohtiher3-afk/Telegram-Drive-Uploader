package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.telegramdrive.uploader.feature.R
import com.telegramdrive.uploader.core.ui.components.GlowBentoGrid
import com.telegramdrive.uploader.core.ui.components.GlowBentoTile
import com.telegramdrive.uploader.core.ui.components.glowBentoVariantForStatus
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.core.ui.components.GlassCard
import com.telegramdrive.uploader.core.ui.components.LiquidGlassEmphasis

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
            DesignTokens.AppColors.onPrimaryContainer
        } else {
            DesignTokens.AppColors.onSecondaryContainer
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
        containerColor = DesignTokens.AppColors.primaryContainer,
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
                        color = DesignTokens.AppColors.onPrimaryContainer
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
                    titleContentColor = DesignTokens.AppColors.onPrimaryContainer,
                    actionIconContentColor = DesignTokens.AppColors.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DesignTokens.AppColors.primaryContainer)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = DesignTokens.AppSpacing.phoneEdge),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
            ) {
        item {
                    TelegramConnectionCard(
                        telegramState = uiState.telegramConnectionState,
                        telegramUserName = displayName.ifBlank { null },
                        telegramUserHandle = uiState.telegramUser?.username,
                        onTelegramConnectClick = onConnectClick,
                        modifier = Modifier
                            .padding(top = DesignTokens.AppSpacing.xs)
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
                        color = DesignTokens.AppColors.onSurface
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.AppSpacing.sm))
                    GlowBentoGrid(
                        tiles = listOf(
                            GlowBentoTile(stringResource(R.string.total_videos), uiState.totalVideosCount.toString(), Icons.Default.VideoLibrary, "stat_total_videos", glowBentoVariantForStatus(false, uiState.pendingCount)),
                            GlowBentoTile(stringResource(R.string.total_size), formatFileSize(uiState.totalSize), Icons.Default.Storage, "stat_total_size", glowBentoVariantForStatus(false, uiState.pendingCount)),
                            GlowBentoTile(stringResource(R.string.pending), uiState.pendingCount.toString(), Icons.Default.Schedule, "stat_pending", glowBentoVariantForStatus(false, uiState.pendingCount)),
                            GlowBentoTile(stringResource(R.string.completed), uiState.completedCount.toString(), Icons.Default.CheckCircle, "stat_completed", glowBentoVariantForStatus(uiState.completedCount > 0, uiState.pendingCount))
                        )
                    )
                }



                item {
                    // Active uploads shown as Telegram-style chat bubbles
                    UploadsChatBubbleList(
                        uploads = uiState.activeUploads,
                        onRetryClicked = { viewModel.retryUpload(it) },
                        onCancelClicked = { viewModel.cancelUpload(it) }
                    )
                }
                

                item { Spacer(modifier = Modifier.height(DesignTokens.AppSpacing.largeSection)) }
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
    val accent = if (tgAuthorized) DesignTokens.AppColors.onPrimaryContainer else DesignTokens.AppColors.onSecondaryContainer

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = MaterialTheme.shapes.large,
        emphasis = LiquidGlassEmphasis.Operational
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.md)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = accent.copy(alpha = 0.2f),
                contentColor = accent
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
                    text = stringResource(R.string.telegram_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DesignTokens.AppColors.onSurface
                )
                Text(
                    text = if (tgAuthorized) {
                        telegramUserName ?: stringResource(R.string.telegram_connected)
                    } else {
                        stringResource(R.string.telegram_not_connected)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = DesignTokens.AppColors.onSurface.copy(alpha = 0.6f)
                )
            }

            if (!tgAuthorized) {
                FilledTonalButton(
                    onClick = onTelegramConnectClick,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = accent.copy(alpha = 0.2f),
                        contentColor = accent
                    )
                ) {
                    Text(stringResource(R.string.connect))
                }
            } else {
                IconButton(
                    onClick = { /* Future: show disconnect dialog */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = DesignTokens.AppColors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadFeatureCard(
    onSelectVideos: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = DesignTokens.AppColors.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(DesignTokens.AppSpacing.medium)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.new_upload),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DesignTokens.AppColors.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.select_files_from_telegram),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.AppColors.onPrimaryContainer
                )
            }
            FilledTonalButton(
                onClick = onSelectVideos,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DesignTokens.AppColors.primary,
                    contentColor = DesignTokens.AppColors.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.AppSpacing.xs))
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
        color = accent.copy(alpha = 0.16f),
        contentColor = DesignTokens.AppColors.onSurface
    ) {
        Text(
            text = activeCount.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = DesignTokens.AppSpacing.sm, vertical = DesignTokens.AppSpacing.xs)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color = DesignTokens.AppColors.onPrimaryContainer
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = MaterialTheme.shapes.large,
        emphasis = LiquidGlassEmphasis.Operational
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignTokens.AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.small)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = accent.copy(alpha = 0.2f),
                contentColor = accent
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
                color = DesignTokens.AppColors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = DesignTokens.AppColors.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
