package com.telegramdrive.uploader.feature.upload

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.text.DateFormat
import java.util.Calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.ErrorState
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.components.glowSignalRim
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay

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

    Scaffold(
        topBar = {
            if (isSelectionMode && uiState is UploadUiState.Success) {
                val successState = uiState as UploadUiState.Success
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.selected) + " ${selectedVideoIds.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { selectedVideoIds = emptySet() },
                            modifier = Modifier.testTag("selection_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear selection"
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                selectedVideoIds = successState.preparedVideos.map { it.id }.toSet()
                            },
                            modifier = Modifier.testTag("selection_select_all_button")
                        ) {
                            Text(
                                "Select All",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = {
                                val videosToRemove = successState.preparedVideos.filter { it.id in selectedVideoIds }
                                viewModel.removePreparedVideos(videosToRemove)
                                selectedVideoIds = emptySet()
                            },
                            modifier = Modifier.testTag("selection_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(com.telegramdrive.uploader.R.string.prepare_videos)) },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("upload_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(com.telegramdrive.uploader.R.string.back)
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UploadUiState.Idle -> {
                    EmptyState(
                        icon = Icons.Default.VideoLibrary,
                        title = stringResource(com.telegramdrive.uploader.R.string.no_videos_selected),
                        supportingText = stringResource(com.telegramdrive.uploader.R.string.select_videos_from_home),
                        actionText = stringResource(com.telegramdrive.uploader.R.string.go_back),
                        onActionClick = onBackClick,
                        modifier = Modifier.testTag("upload_empty_state")
                    )
                }
                is UploadUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("upload_loading"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.extracting_video_metadata),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is UploadUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetryClick = onBackClick,
                        modifier = Modifier.testTag("upload_error_state")
                    )
                }
                is UploadUiState.Success -> {
                    if (state.preparedVideos.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.VideoLibrary,
                            title = stringResource(com.telegramdrive.uploader.R.string.all_videos_removed),
                            supportingText = stringResource(com.telegramdrive.uploader.R.string.select_more_videos),
                            actionText = stringResource(com.telegramdrive.uploader.R.string.go_back),
                            onActionClick = onBackClick,
                            modifier = Modifier.testTag("upload_empty_state")
                        )
                    } else {
                        val totalSize = state.preparedVideos.sumOf { it.fileSize }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Destination Selection
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedDestination != null)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = if (selectedDestination == null) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.telegram_destination_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = selectedDestination?.title ?: stringResource(com.telegramdrive.uploader.R.string.select_target),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    TextButton(onClick = onSelectDestination) {
                                        Text(stringResource(if (selectedDestination == null) com.telegramdrive.uploader.R.string.select_action else com.telegramdrive.uploader.R.string.schedule_change))
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(com.telegramdrive.uploader.R.string.schedule_upload), style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            scheduledAt?.let {
                                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)
                                            } ?: stringResource(com.telegramdrive.uploader.R.string.start_immediately),
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
                                                            viewModel.setScheduledAt(calendar.timeInMillis)
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
                                    ) { Text(stringResource(if (scheduledAt == null) com.telegramdrive.uploader.R.string.schedule_choose else com.telegramdrive.uploader.R.string.schedule_change)) }
                                    if (scheduledAt != null) {
                                        TextButton(onClick = { viewModel.setScheduledAt(null) }) { Text(stringResource(com.telegramdrive.uploader.R.string.clear)) }
                                    }
                                }
                            }

                            // Smart File Assistant
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    FilledTonalButton(
                                        onClick = viewModel::applyAllSmartSuggestions,
                                        enabled = smartSuggestions.isNotEmpty(),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        modifier = Modifier.testTag("smart_file_assistant_button")
                                    ) {
                                        Text(stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_suggest))
                                    }
                                }
                            }
                            // Warning Banner if invalid files were skipped
                            state.invalidFilesWarning?.let { warning ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .testTag("invalid_files_warning_card"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = warning,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // Summary Header
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.videos_selected_summary, state.preparedVideos.size),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.total_size_summary, formatFileSize(totalSize)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // List
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prepared_video_list"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.preparedVideos, key = { it.id }) { video ->
                                    val isSelected = video.id in selectedVideoIds
                                    VideoItem(
                                        video = video,
                                        isSelected = isSelected,
                                        onSelectedChange = { checked ->
                                            selectedVideoIds = if (checked) {
                                                selectedVideoIds + video.id
                                            } else {
                                                selectedVideoIds - video.id
                                            }
                                        },
                                        onRemoveClick = { viewModel.removePreparedVideo(video) },
                                        modifier = Modifier.clickable {
                                            selectedVideoIds = if (isSelected) {
                                                selectedVideoIds - video.id
                                            } else {
                                                selectedVideoIds + video.id
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom Add to Queue Action
                            Button(
                                onClick = {
                                    viewModel.addToQueue(onComplete = onQueueAdded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .glowSignalRim(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        enabled = selectedDestination != null && !state.isSubmitting
                                    )
                                    .testTag("add_to_queue_button"),
                                enabled = selectedDestination != null && !state.isSubmitting,
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
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
                                        text = stringResource(com.telegramdrive.uploader.R.string.add_to_queue),
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
