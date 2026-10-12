package com.telegramdrive.uploader.feature.upload

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.AppCard
import com.telegramdrive.uploader.core.ui.components.AppEmptyState
import com.telegramdrive.uploader.core.ui.components.AppErrorState
import com.telegramdrive.uploader.core.ui.components.Eyebrow
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import com.telegramdrive.uploader.core.util.media.VideoQualityPreset
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.feature.R
import java.text.DateFormat
import java.util.Calendar

/**
 * Upload preparation — design screen (05) "Add files".
 *
 * Same screen, new look: numbered eyebrow, Doto title, Dusk cards,
 * pinned Add-to-queue action. All tags and ViewModel contracts unchanged.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBackClick: () -> Unit,
    onSelectDestination: () -> Unit,
    onQueueAdded: () -> Unit,
    viewModel: UploadViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDestination by viewModel.selectedDestination.collectAsStateWithLifecycle()
    val scheduledAt by viewModel.scheduledAt.collectAsStateWithLifecycle()
    val smartSuggestions by viewModel.smartSuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedVideoIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedVideoIds.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val successState = uiState as? UploadUiState.Success
        if (isSelectionMode) {
            SelectionHeader(
                count = selectedVideoIds.size,
                onClearSelection = { selectedVideoIds = emptySet() },
                onSelectAll = {
                    successState?.let { selectedVideoIds = it.preparedVideos.map { v -> v.id }.toSet() }
                },
                onDelete = {
                    val videosToRemove = successState?.preparedVideos?.filter { it.id in selectedVideoIds }.orEmpty()
                    viewModel.removePreparedVideos(videosToRemove)
                    selectedVideoIds = emptySet()
                }
            )
        } else {
            UploadHeader(onBackClick = onBackClick)
        }

        when (val state = uiState) {
            is UploadUiState.Idle -> {
                AppEmptyState(
                    icon = Icons.Default.VideoLibrary,
                    title = stringResource(R.string.no_videos_selected),
                    supportingText = stringResource(R.string.select_videos_from_home),
                    actionText = stringResource(R.string.go_back),
                    onActionClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("upload_empty_state")
                )
            }
            is UploadUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("upload_loading"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.extracting_video_metadata),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is UploadUiState.Error -> {
                AppErrorState(
                    message = state.message,
                    onRetryClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("upload_error_state")
                )
            }
            is UploadUiState.Success -> {
                if (state.preparedVideos.isEmpty()) {
                    AppEmptyState(
                        icon = Icons.Default.VideoLibrary,
                        title = stringResource(R.string.all_videos_removed),
                        supportingText = stringResource(R.string.select_more_videos),
                        actionText = stringResource(R.string.go_back),
                        onActionClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("upload_empty_state")
                    )
                } else {
                    UploadContent(
                        state = state,
                        selectedDestination = selectedDestination,
                        scheduledAt = scheduledAt,
                        smartSuggestions = smartSuggestions.keys,
                        selectedVideoIds = selectedVideoIds,
                        onSelectDestination = onSelectDestination,
                        onToggleVideo = { id, checked ->
                            selectedVideoIds = if (checked) selectedVideoIds + id else selectedVideoIds - id
                        },
                        onRemoveVideo = viewModel::removePreparedVideo,
                        onSchedulePick = { viewModel.setScheduledAt(it) },
                        onScheduleClear = { viewModel.setScheduledAt(null) },
                        onApplySmartSuggestions = viewModel::applyAllSmartSuggestions,
                        onSetPreset = viewModel::setCompressionPreset,
                        onAddToQueue = { viewModel.addToQueue(onComplete = onQueueAdded) },
                        context = context
                    )
                }
            }
        }
    }
}

/** Selection-mode header: close, count, select all, delete. */
@Composable
private fun SelectionHeader(
    count: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClearSelection,
            modifier = Modifier.testTag("selection_clear_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.clear_selection)
            )
        }
        Text(
            text = stringResource(R.string.selection_selected) + " $count",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onSelectAll,
            modifier = Modifier.testTag("selection_select_all_button")
        ) {
            Text(
                text = stringResource(R.string.select_all),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("selection_delete_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.remove_selected),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Numbered eyebrow + Doto title, with the back affordance. */
@Composable
private fun UploadHeader(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("upload_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
        Eyebrow(text = "(05) NEW UPLOAD")
        Text(
            text = stringResource(R.string.prepare_videos),
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = DotoFamily,
                fontWeight = FontWeight.Black,
                fontSize = 50.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Success content: cards + prepared list + pinned add-to-queue. */
@Composable
private fun UploadContent(
    state: UploadUiState.Success,
    selectedDestination: TelegramDestination?,
    scheduledAt: Long?,
    smartSuggestions: Set<String>,
    selectedVideoIds: Set<String>,
    onSelectDestination: () -> Unit,
    onToggleVideo: (String, Boolean) -> Unit,
    onRemoveVideo: (UploadTask) -> Unit,
    onSchedulePick: (Long) -> Unit,
    onScheduleClear: () -> Unit,
    onApplySmartSuggestions: () -> Unit,
    onSetPreset: (VideoQualityPreset) -> Unit,
    onAddToQueue: () -> Unit,
    context: android.content.Context
) {
    val totalSize = state.preparedVideos.sumOf { it.fileSize }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.medium)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("prepared_video_list"),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            item(key = "destination") {
                DestinationCard(
                    selectedDestination = selectedDestination,
                    onClick = onSelectDestination
                )
            }

            item(key = "schedule") {
                ScheduleCard(
                    scheduledAt = scheduledAt,
                    onPick = onSchedulePick,
                    onClear = onScheduleClear,
                    context = context
                )
            }

            if (smartSuggestions.isNotEmpty()) {
                item(key = "smart") {
                    SmartAssistantCard(
                        enabled = true,
                        onClick = onApplySmartSuggestions
                    )
                }
            }

            state.invalidFilesWarning?.let { warning ->
                item(key = "warning") {
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invalid_files_warning_card"),
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(AppSpacing.medium)
                        )
                    }
                }
            }

            item(key = "compression") {
                CompressionCard(
                    preset = state.compressionPreset,
                    isCompressing = state.isCompressing,
                    onSetPreset = onSetPreset
                )
            }

            item(key = "summary") {
                SummaryCard(
                    count = state.preparedVideos.size,
                    totalSize = totalSize
                )
            }

            items(state.preparedVideos, key = { it.id }) { video ->
                val isSelected = video.id in selectedVideoIds
                VideoItem(
                    video = video,
                    isSelected = isSelected,
                    onSelectedChange = { checked -> onToggleVideo(video.id, checked) },
                    onRemoveClick = { onRemoveVideo(video) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleVideo(video.id, !isSelected) }
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Pinned Add to Queue action.
        Button(
            onClick = onAddToQueue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.medium)
                .testTag("add_to_queue_button"),
            enabled = selectedDestination != null && !state.isSubmitting,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.add_to_queue),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/** Destination selector card. */
@Composable
private fun DestinationCard(
    selectedDestination: TelegramDestination?,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (selectedDestination != null) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.telegram_destination_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedDestination?.title ?: stringResource(R.string.select_target),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onClick) {
                Text(
                    text = stringResource(
                        if (selectedDestination == null) R.string.select_action else R.string.schedule_change
                    )
                )
            }
        }
    }
}

/** Schedule selector card with native date/time dialogs. */
@Composable
private fun ScheduleCard(
    scheduledAt: Long?,
    onPick: (Long) -> Unit,
    onClear: () -> Unit,
    context: android.content.Context
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.schedule_upload),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = scheduledAt?.let {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)
                    } ?: stringResource(R.string.start_immediately),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    onPick(calendar.timeInMillis)
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.testTag("schedule_upload_button")
            ) {
                Text(
                    text = stringResource(
                        if (scheduledAt == null) R.string.schedule_choose else R.string.schedule_change
                    )
                )
            }
            if (scheduledAt != null) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.clear))
                }
            }
        }
    }
}

/** Smart file assistant card. */
@Composable
private fun SmartAssistantCard(
    enabled: Boolean,
    onClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.smart_file_assistant_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.smart_file_assistant_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.testTag("smart_file_assistant_button")
            ) {
                Text(stringResource(R.string.smart_file_assistant_suggest))
            }
        }
    }
}

/** Compression quality selector card. */
@Composable
private fun CompressionCard(
    preset: VideoQualityPreset,
    isCompressing: Boolean,
    onSetPreset: (VideoQualityPreset) -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compression_selector_card")
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.compression_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.compression_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isCompressing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                VideoQualityPreset.values().forEach { p ->
                    FilterChip(
                        selected = preset == p,
                        onClick = { onSetPreset(p) },
                        enabled = !isCompressing,
                        label = {
                            Text(
                                text = when (p) {
                                    VideoQualityPreset.LOW -> stringResource(R.string.compression_low)
                                    VideoQualityPreset.MEDIUM -> stringResource(R.string.compression_medium)
                                    VideoQualityPreset.HIGH -> stringResource(R.string.compression_high)
                                    VideoQualityPreset.ORIGINAL -> stringResource(R.string.compression_original)
                                }
                            )
                        },
                        modifier = Modifier.testTag("compression_${p.name.lowercase()}")
                    )
                }
            }
        }
    }
}

/** Summary header: count + total size. */
@Composable
private fun SummaryCard(count: Int, totalSize: Long) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.medium)
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.videos_selected_summary,
                    count,
                    count
                ),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = DotoFamily,
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.total_size_summary, formatFileSize(totalSize)),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceMonoFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}