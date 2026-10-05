package com.telegramdrive.uploader.feature.queue

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.core.ui.components.GlassCard
import com.telegramdrive.uploader.core.ui.components.LiquidGlassEmphasis
import com.telegramdrive.uploader.core.ui.components.MissionSearchField
import com.telegramdrive.uploader.core.ui.components.MissionSegmentedTabs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DesignTokens.AppColors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignTokens.AppColors.background,
                    titleContentColor = DesignTokens.AppColors.contentPrimary,
                ),
                title = {
                    Column {
                        Text(
                            text = stringResource(com.telegramdrive.uploader.feature.R.string.upload_queue),
                            color = DesignTokens.AppColors.contentPrimary
                        )
                        Text(
                            text = pluralStringResource(
                                com.telegramdrive.uploader.feature.R.plurals.queue_count_summary,
                                uiState.activeCount,
                                uiState.activeCount,
                                uiState.failedCount
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = DesignTokens.AppColors.contentMuted
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
            if (uiState.queueItems.isEmpty() && uiState.selectedFilter == QueueFilter.ALL && uiState.query.isBlank()) {
                EmptyState(
                    icon = Icons.Default.HourglassEmpty,
                    title = stringResource(com.telegramdrive.uploader.feature.R.string.queue_empty_title),
                    supportingText = stringResource(com.telegramdrive.uploader.feature.R.string.queue_empty_supporting),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.AppSpacing.phoneEdge, vertical = DesignTokens.AppSpacing.phoneSection)
                        .testTag("queue_empty_state")
                )
                // The search field and the filter tabs are deliberately rendered above the
                // empty state rather than only inside the populated list. Hiding them made
                // the filters unreachable on a fresh install, which is the one moment a user
                // is most likely to want them.
                SearchAndFilters(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                    selectedFilter = uiState.selectedFilter,
                    onSelectFilter = viewModel::selectFilter,
                    modifier = Modifier.padding(horizontal = DesignTokens.AppSpacing.phoneEdge)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.AppSpacing.phoneEdge)
                        .testTag("queue_list"),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
                ) {
                    item {
                        SearchAndFilters(
                            query = uiState.query,
                            onQueryChange = viewModel::onQueryChanged,
                            selectedFilter = uiState.selectedFilter,
                            onSelectFilter = viewModel::selectFilter
                        )
                    }
                    item {
                        if (uiState.failedCount > 0 || uiState.activeCount > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassOverlay(
                                        shape = MaterialTheme.shapes.large,
                                        accent = DesignTokens.AppColors.purpleHot
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = DesignTokens.AppColors.surfaceCard
                                ),
                                shape = MaterialTheme.shapes.large,
                                border = BorderStroke(
                                    1.dp,
                                    DesignTokens.AppColors.glassBorder
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
                                        text = stringResource(com.telegramdrive.uploader.feature.R.string.queue_controls_title),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DesignTokens.AppColors.purpleHot
                                    )
                                    Text(
                                        text = stringResource(com.telegramdrive.uploader.feature.R.string.queue_controls_supporting),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DesignTokens.AppColors.contentMuted
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (uiState.failedCount > 0) {
                                            FilledTonalButton(
                                                onClick = viewModel::retryAllFailed,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("retry_all_failed"),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(com.telegramdrive.uploader.feature.R.string.retry),
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                            }
                                        }
                                        if (uiState.activeCount > 0) {
                                            FilledTonalButton(
                                                onClick = viewModel::pauseAllActive,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("pause_all_active"),
                                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.PauseCircleOutline,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(com.telegramdrive.uploader.feature.R.string.pause),
                                                    style = MaterialTheme.typography.labelLarge
                                                )
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
                                title = stringResource(com.telegramdrive.uploader.feature.R.string.queue_no_matching_title),
                                supportingText = stringResource(com.telegramdrive.uploader.feature.R.string.queue_no_matching_supporting),
                                modifier = Modifier.testTag("queue_filtered_empty_state")
                            )
                        }
                    } else {
                        // The queue is backed by a Room Flow, so once a frame is emitted the
                        // list is authoritative; there is no separate "loading" phase to show.
                        items(uiState.queueItems, key = { it.id }) { video ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(92.dp),
                                shape = MaterialTheme.shapes.medium,
                                emphasis = LiquidGlassEmphasis.Operational
                            ) {
                                VideoItem(
                                    video = video,
                                    onRemoveClick = { viewModel.removeUpload(video.id) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                UploadStatusIndicator(
                                    video = video,
                                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                                    onPauseClick = { viewModel.pauseUpload(video.id) },
                                    onResumeClick = { viewModel.resumeUpload(video.id) },
                                    onRetryClick = { viewModel.retryUpload(video.id) },
                                    onCancelClick = { viewModel.cancelUpload(video.id) }
                                )
                            }
                        }
                    }
                    // Either a non-blank search or a non-ALL filter can empty the list, and both need
                    // the same explanation. Keying this off the query alone left the filtered
                    // case showing a bare list with no message at all.
                    if (uiState.queueItems.isEmpty() &&
                        (uiState.query.isNotBlank() || uiState.selectedFilter != QueueFilter.ALL)
                    ) {
                        item {
                            Text(
                                text = stringResource(
                                    if (uiState.query.isNotBlank()) {
                                        com.telegramdrive.uploader.feature.R.string.queue_no_matching_title
                                    } else {
                                        com.telegramdrive.uploader.feature.R.string.queue_filtered_empty_title
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = DesignTokens.AppSpacing.md)
                                    .testTag("queue_no_results"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = DesignTokens.AppColors.contentMuted
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }
    }
}

/**
 * Search field plus the four filter tabs.
 *
 * Kept as one composable because both the empty and the populated branch render it, and a
 * copy that drifts between the two is exactly the kind of difference no test notices. The
 * `phoneEdge` padding is applied only by the empty-state caller; the list branch already
 * pads its own content.
 */
@Composable
private fun SearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: QueueFilter,
    onSelectFilter: (QueueFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MissionSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(com.telegramdrive.uploader.feature.R.string.filter_active_queue),
            modifier = Modifier.fillMaxWidth(),
            textFieldModifier = Modifier.testTag("queue_search_field")
        )
        Spacer(modifier = Modifier.height(4.dp))
        MissionSegmentedTabs(
            options = QueueFilter.entries.map { filterLabel(it) },
            selectedIndex = QueueFilter.entries.indexOf(selectedFilter),
            onSelect = { onSelectFilter(QueueFilter.entries[it]) },
            modifier = Modifier.testTag("queue_filters"),
            testTagPrefix = "queue_filter",
            optionTags = QueueFilter.entries.map { it.name.lowercase() }
        )
    }
}

@Composable
private fun filterLabel(filter: QueueFilter): String = when (filter) {
    QueueFilter.ALL -> stringResource(com.telegramdrive.uploader.feature.R.string.queue_filter_all)
    QueueFilter.ACTIVE -> stringResource(com.telegramdrive.uploader.feature.R.string.queue_filter_active)
    QueueFilter.PAUSED -> stringResource(com.telegramdrive.uploader.feature.R.string.queue_filter_paused)
    QueueFilter.FAILED -> stringResource(com.telegramdrive.uploader.feature.R.string.queue_filter_failed)
}
