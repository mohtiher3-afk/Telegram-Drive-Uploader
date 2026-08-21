package com.telegramdrive.uploader.feature.queue

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(com.telegramdrive.uploader.R.string.upload_queue))
                        Text(
                            text = "${uiState.activeCount} active · ${uiState.failedCount} failed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            if (uiState.queueItems.isEmpty() && uiState.selectedFilter == QueueFilter.ALL) {
                EmptyState(
                    icon = Icons.Default.HourglassEmpty,
                    title = "Your queue is empty",
                    supportingText = "Select videos from Home to start a reliable background upload.",
                    modifier = Modifier.testTag("queue_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("queue_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QueueFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = uiState.selectedFilter == filter,
                                    onClick = { viewModel.selectFilter(filter) },
                                    label = { Text(filterLabel(filter)) },
                                    modifier = Modifier.testTag("queue_filter_${filter.name.lowercase()}")
                                )
                            }
                        }
                    }
                    item {
                        if (uiState.failedCount > 0 || uiState.activeCount > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Queue controls",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "Manage uploads without opening each item.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    if (uiState.failedCount > 0) {
                                        Button(
                                            onClick = viewModel::retryAllFailed,
                                            modifier = Modifier.testTag("retry_all_failed")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                            Text("Retry")
                                        }
                                    }
                                    if (uiState.activeCount > 0) {
                                        Button(
                                            onClick = viewModel::pauseAllActive,
                                            modifier = Modifier.testTag("pause_all_active")
                                        ) {
                                            Icon(Icons.Default.PauseCircleOutline, contentDescription = null)
                                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                            Text("Pause")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (uiState.queueItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.HourglassEmpty,
                                title = "No matching uploads",
                                supportingText = "Try another filter to see more queue items.",
                                modifier = Modifier.testTag("queue_filtered_empty_state")
                            )
                        }
                    } else {
                        items(uiState.queueItems, key = { it.id }) { video ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                VideoItem(
                                    video = video,
                                    onRemoveClick = { viewModel.removeUpload(video.id) }
                                )
                                UploadStatusIndicator(
                                    video = video,
                                    modifier = Modifier.padding(top = 4.dp),
                                    onPauseClick = { viewModel.pauseUpload(video.id) },
                                    onResumeClick = { viewModel.resumeUpload(video.id) },
                                    onRetryClick = { viewModel.retryUpload(video.id) },
                                    onCancelClick = { viewModel.cancelUpload(video.id) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

private fun filterLabel(filter: QueueFilter): String = when (filter) {
    QueueFilter.ALL -> "All"
    QueueFilter.ACTIVE -> "Active"
    QueueFilter.PAUSED -> "Paused"
    QueueFilter.FAILED -> "Failed"
}
