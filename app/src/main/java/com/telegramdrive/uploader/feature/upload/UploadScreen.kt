package com.telegramdrive.uploader.feature.upload

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VideoLibrary
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prepare Videos") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("upload_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                                )
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
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    TextButton(onClick = onSelectDestination) {
                                        Text(if (selectedDestination == null) "Select" else "Change")
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
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "${state.preparedVideos.size} videos selected",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                                    .height(50.dp)
                                    .testTag("add_to_queue_button"),
                                enabled = selectedDestination != null,
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
