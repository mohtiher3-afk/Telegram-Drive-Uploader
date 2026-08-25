package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.R
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import com.telegramdrive.uploader.domain.model.TelegramConnectionState

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
    val connectionAccent by animateColorAsState(
        targetValue = if (uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        },
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "connection_accent"
    )

    val secondaryGlow = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)

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
                    Column {
                        Text(
                            text = stringResource(R.string.telegram_drive),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.active_uploads),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                connectionAccent.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension * 0.72f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.08f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                secondaryGlow,
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension * 0.55f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.88f)
                    )
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                item {
                    ConnectionCard(
                        state = uiState.telegramConnectionState,
                        userName = uiState.telegramUser?.let { user ->
                            "${user.firstName} ${user.lastName ?: ""}".trim()
                        },
                        username = uiState.telegramUser?.username,
                        onConnectClick = onConnectClick,
                        modifier = Modifier
                            .padding(top = AppSpacing.sm)
                            .testTag("telegram_status_card")
                    )
                }

                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = AppMotion.shortTween(motionEnabled))
                            .testTag("upload_hero_card"),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.lg),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.upload_videos),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    Text(
                                        text = stringResource(R.string.select_videos_description),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = stringResource(R.string.upload_cloud_icon),
                                    tint = connectionAccent,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    pickerLauncher.launch(arrayOf("*/*"))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("select_videos_button")
                            ) {
                                Text(stringResource(R.string.select_videos))
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.local_statistics),
                        style = MaterialTheme.typography.titleMedium,
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
                            modifier = Modifier.weight(1f).testTag("stat_total_videos")
                        )
                        StatCard(
                            title = stringResource(R.string.total_size),
                            value = formatFileSize(uiState.totalSize),
                            modifier = Modifier.weight(1f).testTag("stat_total_size")
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
                            modifier = Modifier.weight(1f).testTag("stat_pending")
                        )
                        StatCard(
                            title = stringResource(R.string.completed),
                            value = uiState.completedCount.toString(),
                            modifier = Modifier.weight(1f).testTag("stat_completed")
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.active_uploads),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (uiState.activeUploads.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.no_active_uploads),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.lg)
                                    .testTag("no_active_uploads_text")
                            )
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

                item { Spacer(modifier = Modifier.height(AppSpacing.section)) }
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    state: TelegramConnectionState,
    userName: String?,
    username: String?,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authorized = state == TelegramConnectionState.AUTHORIZED
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (authorized) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = stringResource(R.string.connected_status_description),
                tint = if (authorized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(30.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(if (authorized) R.string.telegram_connected else R.string.telegram_not_connected),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (authorized) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (authorized) {
                        userName ?: stringResource(R.string.user)
                    } else {
                        stringResource(R.string.connect_account)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (authorized) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (authorized) {
                    username?.let {
                        Text(
                            text = "@$it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.72f)
                        )
                    }
                }
            }
            if (!authorized) {
                FilledTonalButton(onClick = onConnectClick) {
                    Text(stringResource(R.string.connect))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
