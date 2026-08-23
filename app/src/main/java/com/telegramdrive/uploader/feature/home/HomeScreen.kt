package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.domain.model.TelegramConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onVideosSelected: (List<Uri>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Video Mime Types Filter Contract
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                onVideosSelected(uris)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.telegramdrive.uploader.R.string.telegram_drive)) },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(com.telegramdrive.uploader.R.string.settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            // Telegram Connection Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm)
                        .testTag("telegram_status_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        if (uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Connected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.telegram_connected),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    val user = uiState.telegramUser
                                    val name = if (user != null) "${user.firstName} ${user.lastName ?: ""}".trim() else stringResource(com.telegramdrive.uploader.R.string.user)
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    user?.username?.let {
                                        Text(
                                            text = "@$it",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.telegram_not_connected),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.connect_account),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onConnectClick,
                                modifier = Modifier.testTag("home_connect_telegram_button")
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.connect))
                            }
                        }
                    }
                }
            }

            // 1. Upload Hero Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.sm)
                        .testTag("upload_hero_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = stringResource(com.telegramdrive.uploader.R.string.upload_cloud_icon),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.upload_videos),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.select_videos_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = {
                                pickerLauncher.launch(
                                    // Use a broad picker filter because providers often report MKV, TS, AVI,
                                    // and camera formats as application/octet-stream. The extractor validates
                                    // the selected extension/MIME and rejects non-video files safely.
                                    arrayOf("*/*")
                                )
                            },
                            modifier = Modifier.testTag("select_videos_button")
                        ) {
                            Text(stringResource(com.telegramdrive.uploader.R.string.select_videos))
                        }
                    }
                }
            }

            // 2. Statistics Grid
            item {
                Text(
                    text = stringResource(com.telegramdrive.uploader.R.string.local_statistics),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = stringResource(com.telegramdrive.uploader.R.string.total_videos),
                            value = uiState.totalVideosCount.toString(),
                            modifier = Modifier.weight(1f).testTag("stat_total_videos")
                        )
                        StatCard(
                            title = stringResource(com.telegramdrive.uploader.R.string.total_size),
                            value = formatFileSize(uiState.totalSize),
                            modifier = Modifier.weight(1f).testTag("stat_total_size")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = stringResource(com.telegramdrive.uploader.R.string.pending),
                            value = uiState.pendingCount.toString(),
                            modifier = Modifier.weight(1f).testTag("stat_pending")
                        )
                        StatCard(
                            title = stringResource(com.telegramdrive.uploader.R.string.completed),
                            value = uiState.completedCount.toString(),
                            modifier = Modifier.weight(1f).testTag("stat_completed")
                        )
                    }
                }
            }

            // 3. Active Uploads List
            item {
                Text(
                    text = stringResource(com.telegramdrive.uploader.R.string.active_uploads),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.activeUploads.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(com.telegramdrive.uploader.R.string.no_active_uploads),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("no_active_uploads_text")
                            )
                        }
                    }
                }
            } else {
                items(uiState.activeUploads, key = { "active_${it.id}" }) { upload ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        VideoItem(video = upload)
                        UploadStatusIndicator(
                            video = upload,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Margin bottom
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

