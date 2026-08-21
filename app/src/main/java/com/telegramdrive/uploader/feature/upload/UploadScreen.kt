package com.telegramdrive.uploader.feature.upload

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.text.DateFormat
import java.util.Calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    Scaffold(
        topBar = {
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
                        title = "No videos selected",
                        supportingText = "Please select videos from the Home tab to begin preparation.",
                        actionText = "Go Back",
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
                            text = "Extracting video metadata...",
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
                            title = "All videos removed",
                            supportingText = "Select more videos from the Home tab to prepare them.",
                            actionText = "Go Back",
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
                                onClick = onSelectDestination,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("select_destination_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedDestination != null)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = MaterialTheme.shapes.large,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                            text = "Telegram Destination",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = selectedDestination?.title ?: "Select Target...",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    TextButton(onClick = onSelectDestination) {
                                        Text(if (selectedDestination == null) "Select" else "Change")
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
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                            } ?: "Start immediately",
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
                                    ) { Text(if (scheduledAt == null) "Choose" else "Change") }
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
                                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
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
                            // Summary Header
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "${state.preparedVideos.size} videos selected",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Total size: ${formatFileSize(totalSize)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
                                    VideoItem(
                                        video = video,
                                        onRemoveClick = { viewModel.removePreparedVideo(video) }
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
                                    .testTag("add_to_queue_button"),
                                enabled = selectedDestination != null,
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Add to Queue",
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
