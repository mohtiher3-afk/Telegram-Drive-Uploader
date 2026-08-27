package com.telegramdrive.uploader.feature.history

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
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import java.util.Locale

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
                    title = stringResource(com.telegramdrive.uploader.R.string.history_no_uploads),
                    supportingText = stringResource(com.telegramdrive.uploader.R.string.history_no_uploads_supporting),
                    modifier = Modifier
                        .padding(horizontal = AppSpacing.phoneEdge, vertical = AppSpacing.phoneSection)
                        .testTag("history_empty_state")
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.phoneEdge)
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassOverlay(
                                    shape = MaterialTheme.shapes.large,
                                    accent = MaterialTheme.colorScheme.primary
                                ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(
                                        com.telegramdrive.uploader.R.string.history_matches_summary,
                                        uiState.totalMatches,
                                        formatFileSize(uiState.totalSize)
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row {
                                    TextButton(onClick = { viewModel.setSort(HistorySort.NEWEST) }) { Text(stringResource(com.telegramdrive.uploader.R.string.newest)) }
                                    TextButton(onClick = { viewModel.setSort(HistorySort.LARGEST) }) { Text(stringResource(com.telegramdrive.uploader.R.string.largest)) }
                                }
                            }
                        }
                    }
                    if (uiState.historyItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.History,
                                title = stringResource(com.telegramdrive.uploader.R.string.history_no_matching),
                                supportingText = stringResource(com.telegramdrive.uploader.R.string.history_no_matching_supporting),
                                modifier = Modifier.testTag("history_filtered_empty_state")
                            )
                        }
                    } else {
                        items(uiState.historyItems, key = { it.id }) { video ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                    VideoItem(
                                        video = video,
                                        onRemoveClick = { viewModel.deleteUpload(video.id) }
                                    )
                                    if (video.uploadDurationMs > 0L) {
                                        Text(
                                            text = stringResource(
                                                com.telegramdrive.uploader.R.string.upload_time,
                                                formatElapsedUploadTime(video.uploadDurationMs)
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    UploadStatusIndicator(
                                        video = video,
                                        modifier = Modifier.padding(top = 4.dp)
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
private fun periodLabel(period: HistoryPeriod): String = when (period) {
    HistoryPeriod.ALL -> stringResource(com.telegramdrive.uploader.R.string.history_period_all)
    HistoryPeriod.TODAY -> stringResource(com.telegramdrive.uploader.R.string.history_period_today)
    HistoryPeriod.LAST_7_DAYS -> stringResource(com.telegramdrive.uploader.R.string.history_period_7_days)
    HistoryPeriod.LAST_30_DAYS -> stringResource(com.telegramdrive.uploader.R.string.history_period_30_days)
}

private fun formatElapsedUploadTime(durationMs: Long): String {
    val totalSeconds = (durationMs / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return if (minutes > 0L) {
        "%dm %02ds".format(Locale.US, minutes, seconds)
    } else {
        "%ds".format(Locale.US, seconds)
    }
}
