package com.telegramdrive.uploader.feature.queue

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
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
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.core.ui.theme.AppSpacing

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
                            text = stringResource(
                                com.telegramdrive.uploader.R.string.queue_count_summary,
                                uiState.activeCount,
                                uiState.failedCount
                            ),
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
                    title = stringResource(com.telegramdrive.uploader.R.string.queue_empty_title),
                    supportingText = stringResource(com.telegramdrive.uploader.R.string.queue_empty_supporting),
                    modifier = Modifier
                        .padding(horizontal = AppSpacing.phoneEdge, vertical = AppSpacing.phoneSection)
                        .testTag("queue_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.phoneEdge)
                        .testTag("queue_list"),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassOverlay(
                                        shape = MaterialTheme.shapes.large,
                                        accent = MaterialTheme.colorScheme.secondary
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                shape = MaterialTheme.shapes.large,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.queue_controls_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.R.string.queue_controls_supporting),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                    if (uiState.failedCount > 0) {
                                        Button(
                                            onClick = viewModel::retryAllFailed,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("retry_all_failed")
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                            Text(stringResource(com.telegramdrive.uploader.R.string.retry))
                                        }
                                    }
                                    if (uiState.activeCount > 0) {
                                        Button(
                                            onClick = viewModel::pauseAllActive,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("pause_all_active")
                                        ) {
                                            Icon(Icons.Default.PauseCircleOutline, contentDescription = null)
                                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                                            Text(stringResource(com.telegramdrive.uploader.R.string.pause))
                                        }
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
                                title = stringResource(com.telegramdrive.uploader.R.string.queue_no_matching_title),
                                supportingText = stringResource(com.telegramdrive.uploader.R.string.queue_no_matching_supporting),
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

@Composable
private fun filterLabel(filter: QueueFilter): String = when (filter) {
    QueueFilter.ALL -> stringResource(com.telegramdrive.uploader.R.string.queue_filter_all)
    QueueFilter.ACTIVE -> stringResource(com.telegramdrive.uploader.R.string.queue_filter_active)
    QueueFilter.PAUSED -> stringResource(com.telegramdrive.uploader.R.string.queue_filter_paused)
    QueueFilter.FAILED -> stringResource(com.telegramdrive.uploader.R.string.queue_filter_failed)
}
