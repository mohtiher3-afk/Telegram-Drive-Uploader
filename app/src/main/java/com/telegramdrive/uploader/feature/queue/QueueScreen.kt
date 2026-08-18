package com.telegramdrive.uploader.feature.queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Queue") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.queueItems.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.HourglassEmpty,
                    title = "Your Queue is Empty",
                    supportingText = "Go to the Home tab and select videos to add them here.",
                    modifier = Modifier.testTag("queue_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .testTag("queue_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${uiState.queueItems.size} items pending",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
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
            }
        }
    }
}
