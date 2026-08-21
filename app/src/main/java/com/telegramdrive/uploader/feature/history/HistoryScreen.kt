package com.telegramdrive.uploader.feature.history

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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.telegramdrive.uploader.R.string.upload_history)) },
                actions = {
                    if (uiState.totalMatches > 0) {
                        IconButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(com.telegramdrive.uploader.R.string.clear_history))
                        }
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
            if (uiState.totalMatches == 0 && uiState.query.isBlank() && uiState.period == HistoryPeriod.ALL) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No upload history",
                    supportingText = "Completed uploads will appear here with searchable metadata.",
                    modifier = Modifier.testTag("history_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = viewModel::onQueryChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_search_field"),
                            singleLine = true,
                            label = { Text(stringResource(com.telegramdrive.uploader.R.string.search_file_names)) },
                            placeholder = { Text(stringResource(com.telegramdrive.uploader.R.string.filter_completed_uploads)) }
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryPeriod.values().forEach { option ->
                                FilterChip(
                                    selected = uiState.period == option,
                                    onClick = { viewModel.setPeriod(option) },
                                    label = { Text(periodLabel(option)) },
                                    modifier = Modifier.testTag("history_period_${option.name.lowercase()}")
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${uiState.totalMatches} matches · ${formatFileSize(uiState.totalSize)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                TextButton(onClick = { viewModel.setSort(HistorySort.NEWEST) }) { Text(stringResource(com.telegramdrive.uploader.R.string.newest)) }
                                TextButton(onClick = { viewModel.setSort(HistorySort.LARGEST) }) { Text(stringResource(com.telegramdrive.uploader.R.string.largest)) }
                            }
                        }
                    }
                    if (uiState.historyItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.History,
                                title = "No matching uploads",
                                supportingText = "Try a different name or time range.",
                                modifier = Modifier.testTag("history_filtered_empty_state")
                            )
                        }
                    } else {
                        items(uiState.historyItems, key = { it.id }) { video ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    VideoItem(
                                        video = video,
                                        onRemoveClick = { viewModel.deleteUpload(video.id) }
                                    )
                                    UploadStatusIndicator(
                                        video = video,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

private fun periodLabel(period: HistoryPeriod): String = when (period) {
    HistoryPeriod.ALL -> "All time"
    HistoryPeriod.TODAY -> "Today"
    HistoryPeriod.LAST_7_DAYS -> "7 days"
    HistoryPeriod.LAST_30_DAYS -> "30 days"
}
