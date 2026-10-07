package com.telegramdrive.uploader.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.animation.AnimatedFadeIn
import com.telegramdrive.uploader.core.ui.animation.AnimatedGlassCard
import com.telegramdrive.uploader.core.ui.animation.AnimatedProgressIndicator
import com.telegramdrive.uploader.core.ui.animation.AnimatedRecentUploadItem
import com.telegramdrive.uploader.core.ui.animation.AnimatedStatsGrid
import com.telegramdrive.uploader.core.ui.animation.AnimatedStatCard
import com.telegramdrive.uploader.core.ui.components.Eyebrow
import com.telegramdrive.uploader.core.ui.components.MissionCard
import com.telegramdrive.uploader.core.ui.components.MissionHeroCard
import com.telegramdrive.uploader.core.ui.components.MissionProgressBar
import com.telegramdrive.uploader.core.ui.components.MissionScreen
import com.telegramdrive.uploader.core.ui.components.MissionStat
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.feature.R


/**
 * Dashboard screen, rebuilt against the Mission Control design.
 *
 * Layout follows the design's order: an eyebrow-and-heading block, the Telegram
 * connection card, the quick actions, a two-by-two statistics grid, then the active
 * transfer and the recent-activity list.
 *
 * Every colour comes from [DesignTokens.AppColors] through the shared Mission
 * components. No hex value and no hand-composed alpha appears in this file, which is what
 * lets AppColorsContrastTest stand in for a rendered-UI contrast check.
 *
 * Presentation only: the signature, the ViewModel and every callback are unchanged, and
 * no upload state or business logic is touched.
 */
@Composable
fun HomeScreen(
    onSettingsClick: () -> Unit,
    onConnectClick: () -> Unit,
    onVideosSelected: (List<Uri>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authorized = uiState.telegramConnectionState == TelegramConnectionState.AUTHORIZED
    val displayName = uiState.telegramUser?.let { user ->
        "${user.firstName} ${user.lastName ?: ""}".trim()
    }.orEmpty()

    // Use Photo Picker on Android 13+ (no storage permission needed), fallback to OpenMultipleDocuments
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris ->
            if (uris.isNotEmpty()) onVideosSelected(uris)
        }
    )
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) onVideosSelected(uris)
        }
    )

    MissionScreen {
        LazyColumn(
            modifier = Modifier.testTag("home_screen"),
            contentPadding = PaddingValues(
                start = AppSpacing.medium,
                end = AppSpacing.medium,
                top = AppSpacing.large,
                // Double the bottom inset so the last row clears the navigation bar.
                bottom = AppSpacing.extraLarge * 2
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            item("header") {
                AnimatedFadeIn(visible = true) {
                    HomeHeader(
                        greeting = if (displayName.isNotBlank()) {
                            stringResource(R.string.home_greeting, displayName)
                        } else {
                            stringResource(R.string.telegram_drive)
                        },
                        onSettingsClick = onSettingsClick
                    )
                }
            }

            item("connection") {
                TelegramConnectionCard(
                    authorized = authorized,
                    userName = displayName.ifBlank { null },
                    onConnectClick = onConnectClick,
                    modifier = Modifier.testTag("telegram_status_card")
                )
            }

            item("quick_actions") {
                UploadFeatureCard(
                    onSelectVideos = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    mediaType = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                )
                            )
                        } else {
                            legacyPickerLauncher.launch(arrayOf("*/*"))
                        }
                    },
                    connectEnabled = !authorized,
                    onConnectClick = onConnectClick,
                    modifier = Modifier.testTag("upload_hero_card")
                )
            }

            item("snapshot") {
                SectionHeading(
                    title = stringResource(R.string.home_upload_snapshot),
                    caption = stringResource(R.string.home_snapshot_caption)
                )
            }

            item("stats") {
                AnimatedStatsGrid(
                    totalVideos = uiState.totalVideosCount.toString(),
                    totalSize = formatFileSize(uiState.totalSize),
                    pending = uiState.pendingCount.toString(),
                    completed = uiState.completedCount.toString()
                ) {
                    StatsGrid(
                        totalVideos = uiState.totalVideosCount.toString(),
                        totalSize = formatFileSize(uiState.totalSize),
                        pending = uiState.pendingCount.toString(),
                        completed = uiState.completedCount.toString()
                    )
                }
            }

            val activeUploads = uiState.activeUploads
            if (activeUploads.isNotEmpty()) {
                item("active_heading") {
                    SectionHeading(
                        title = stringResource(R.string.home_active_transfers),
                        caption = stringResource(R.string.home_active_caption)
                    )
                }
                item("active") {
                    MissionHeroCard(modifier = Modifier.testTag("active_transfer_card")) {
                        ActiveTransferSummary(
                            fileName = activeUploads.first().fileName,
                            completedCount = uiState.completedCount,
                            pendingCount = uiState.pendingCount,
                            total = activeUploads.size + uiState.completedCount
                        )
                    }
                }
            }

            val recent = uiState.recentActivity
            if (recent.isNotEmpty()) {
                item("recent_heading") {
                    SectionHeading(
                        title = stringResource(R.string.home_recent_uploads),
                        caption = stringResource(R.string.home_recent_caption)
                    )
                }
                items(recent, key = { it.id }) { upload ->
                    AnimatedRecentUploadItem(
                        fileName = upload.fileName,
                        sizeLabel = formatFileSize(upload.fileSize),
                        completed = upload.status == UploadStatus.COMPLETED,
                        index = recent.indexOf(upload)
                    ) {
                        RecentUploadRow(
                            fileName = upload.fileName,
                            sizeLabel = formatFileSize(upload.fileSize),
                            completed = upload.status == UploadStatus.COMPLETED
                        )
                    }
                }
            }
        }
    }
}

/** Eyebrow, screen title, and the settings affordance. */
@Composable
private fun HomeHeader(
    greeting: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Eyebrow(text = stringResource(R.string.home_eyebrow))
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                color = DesignTokens.AppColors.contentPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.testTag("home_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = DesignTokens.AppColors.contentMuted
            )
        }
    }
}

/** Telegram connection state, with a lime action while the account is not linked. */
@Composable
private fun TelegramConnectionCard(
    authorized: Boolean,
    userName: String?,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (authorized) {
        DesignTokens.AppColors.teal
    } else {
        DesignTokens.AppColors.contentMuted
    }
    MissionCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.telegram_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = DesignTokens.AppColors.contentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (authorized) {
                        userName ?: stringResource(R.string.telegram_connected)
                    } else {
                        stringResource(R.string.telegram_not_connected)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignTokens.AppColors.contentMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!authorized) {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.testTag("connect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DesignTokens.AppColors.lime,
                        contentColor = DesignTokens.AppColors.onLime
                    )
                ) {
                    Text(text = stringResource(R.string.connect))
                }
            }
        }
    }
}

/** The design's quick-action row: a lime primary action and a glass secondary. */
@Composable
private fun UploadFeatureCard(
    onSelectVideos: () -> Unit,
    connectEnabled: Boolean,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MissionCard(modifier = modifier) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            Text(
                text = stringResource(R.string.new_upload),
                style = MaterialTheme.typography.labelLarge,
                color = DesignTokens.AppColors.purpleHot
            )
            Text(
                text = stringResource(R.string.select_files_from_telegram),
                style = MaterialTheme.typography.titleLarge,
                color = DesignTokens.AppColors.contentPrimary,
                modifier = Modifier.padding(top = AppSpacing.xSmall)
            )
            Row(
                modifier = Modifier.padding(top = AppSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Button(
                    onClick = onSelectVideos,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("select_files_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DesignTokens.AppColors.lime,
                        contentColor = DesignTokens.AppColors.onLime
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.select),
                        modifier = Modifier.padding(start = AppSpacing.small)
                    )
                }
                Button(
                    onClick = onConnectClick,
                    enabled = connectEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DesignTokens.AppColors.glassFill,
                        contentColor = DesignTokens.AppColors.contentPrimary
                    )
                ) {
                    Text(text = stringResource(R.string.connect))
                }
            }
        }
    }
}

/** Section title with the small violet caption the design places above each group. */
@Composable
private fun SectionHeading(
    title: String,
    caption: String
) {
    Column(modifier = Modifier.padding(top = AppSpacing.small)) {
        Eyebrow(text = caption)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = DesignTokens.AppColors.contentPrimary
        )
    }
}

/** Two-by-two statistics grid with animated stat cards. */
@Composable
private fun StatsGrid(
    totalVideos: String,
    totalSize: String,
    pending: String,
    completed: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            AnimatedStatCard(
                value = totalVideos,
                label = stringResource(R.string.total_videos),
                index = 0,
                modifier = Modifier.weight(1f)
            )
            AnimatedStatCard(
                value = totalSize,
                label = stringResource(R.string.total_size),
                index = 1,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            AnimatedStatCard(
                value = pending,
                label = stringResource(R.string.pending),
                index = 2,
                modifier = Modifier.weight(1f)
            )
            AnimatedStatCard(
                value = completed,
                label = stringResource(R.string.completed),
                index = 3,
                modifier = Modifier.weight(1f),
                accentColor = DesignTokens.AppColors.lime
            )
        }
    }
}

/** Hero summary of what is currently in flight, with animated progress. */
@Composable
private fun ActiveTransferSummary(
    fileName: String,
    completedCount: Int,
    pendingCount: Int,
    total: Int
) {
    val progress = if (total <= 0) 0f else completedCount.toFloat() / total.toFloat()
    Column(modifier = Modifier.padding(AppSpacing.medium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.titleMedium,
                color = DesignTokens.AppColors.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = DesignTokens.AppColors.lime,
                modifier = Modifier.padding(start = AppSpacing.small)
            )
        }
        AnimatedProgressIndicator(
            progress = progress,
            modifier = Modifier.padding(top = AppSpacing.small),
            color = DesignTokens.AppColors.lime
        )
        Text(
            text = pluralStringResource(R.plurals.home_pending_summary, pendingCount, pendingCount),
            style = MaterialTheme.typography.bodySmall,
            color = DesignTokens.AppColors.contentMuted,
            modifier = Modifier.padding(top = AppSpacing.small)
        )
    }
}

/** One row of the recent-uploads list with animated entry. */
@Composable
private fun RecentUploadRow(
    fileName: String,
    sizeLabel: String,
    completed: Boolean,
    index: Int = 0
) {
    MissionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppSpacing.medium)
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DesignTokens.AppColors.contentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = sizeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = DesignTokens.AppColors.contentMuted
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (completed) {
                    DesignTokens.AppColors.lime
                } else {
                    DesignTokens.AppColors.contentMuted
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}