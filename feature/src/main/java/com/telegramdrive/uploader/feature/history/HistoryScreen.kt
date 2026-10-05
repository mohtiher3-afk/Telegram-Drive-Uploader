package com.telegramdrive.uploader.feature.history

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.telegramdrive.uploader.core.ui.components.MissionBanner
import com.telegramdrive.uploader.core.ui.components.MissionSearchField
import com.telegramdrive.uploader.core.ui.components.MissionSegmentedTabs
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DesignTokens.AppColors.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignTokens.AppColors.background,
                    titleContentColor = DesignTokens.AppColors.contentPrimary,
                    actionIconContentColor = DesignTokens.AppColors.contentMuted,
                ),
                title = {
                    Text(
                        text = stringResource(com.telegramdrive.uploader.feature.R.string.upload_history),
                        color = DesignTokens.AppColors.contentPrimary
                    )
                },
                actions = {
                    if (uiState.totalMatches > 0) {
                        IconButton(
                            onClick = { viewModel.clearHistory() },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(com.telegramdrive.uploader.feature.R.string.clear_history))
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
                    title = stringResource(com.telegramdrive.uploader.feature.R.string.history_no_uploads),
                    supportingText = stringResource(com.telegramdrive.uploader.feature.R.string.history_no_uploads_supporting),
                    modifier = Modifier
                        .padding(horizontal = DesignTokens.AppSpacing.phoneEdge, vertical = DesignTokens.AppSpacing.phoneSection)
                        .testTag("history_empty_state")
                )
                // Same reasoning as the queue screen: the search box and the period tabs
                // belong to the screen, not to the list. Hiding them on an empty history
                // left the filters unreachable on a fresh install.
                SearchAndPeriods(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                    period = uiState.period,
                    onPeriodChange = viewModel::setPeriod,
                    modifier = Modifier.padding(horizontal = DesignTokens.AppSpacing.phoneEdge)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = DesignTokens.AppSpacing.phoneEdge)
                        .testTag("history_list"),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SearchAndPeriods(
                            query = uiState.query,
                            onQueryChange = viewModel::onQueryChanged,
                            period = uiState.period,
                            onPeriodChange = viewModel::setPeriod
                        )
                    }
                    item {
                        MissionBanner(
                            headline = pluralStringResource(
                                com.telegramdrive.uploader.feature.R.plurals.history_matches_summary,
                                uiState.totalMatches,
                                uiState.totalMatches,
                                formatFileSize(uiState.totalSize)
                            ),
                            supporting = stringResource(com.telegramdrive.uploader.feature.R.string.history_banner_supporting),
                            icon = Icons.Default.History,
                            modifier = Modifier.testTag("history_banner"),
                            action = null
                        )
                    }
                    item {
                        MissionSortToggle(
                            sort = uiState.sort,
                            onSortChange = viewModel::setSort,
                            modifier = Modifier.testTag("history_sort")
                        )
                    }
                    if (uiState.historyItems.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Default.History,
                                title = stringResource(com.telegramdrive.uploader.feature.R.string.history_no_matching),
                                supportingText = stringResource(com.telegramdrive.uploader.feature.R.string.history_no_matching_supporting),
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
                                                com.telegramdrive.uploader.feature.R.string.upload_time,
                                                formatElapsedUploadTime(video.uploadDurationMs)
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DesignTokens.AppColors.onSurface.copy(alpha = 0.6f),
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

/**
 * Search field plus the period tabs, shared by the empty and populated branches.
 *
 * Extracted so the two call sites cannot drift, which is the same reason the queue screen
 * has its own `SearchAndFilters`.
 */
@Composable
private fun SearchAndPeriods(
    query: String,
    onQueryChange: (String) -> Unit,
    period: HistoryPeriod,
    onPeriodChange: (HistoryPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MissionSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(com.telegramdrive.uploader.feature.R.string.filter_completed_uploads),
            modifier = Modifier.fillMaxWidth(),
            textFieldModifier = Modifier.testTag("history_search_field")
        )
        MissionSegmentedTabs(
            options = HistoryPeriod.entries.map { periodLabel(it) },
            selectedIndex = HistoryPeriod.entries.indexOf(period),
            onSelect = { onPeriodChange(HistoryPeriod.entries[it]) },
            modifier = Modifier.testTag("history_periods"),
            testTagPrefix = "history_period",
            optionTags = HistoryPeriod.entries.map { it.name.lowercase() }
        )
    }
}

/**
 * Newest / Largest toggle, rendered as a two-segment control so it reads as one choice
 * rather than two independent chips. Selecting the active option is a no-op rather than
 * an error, which keeps the toggle usable by screen readers and by tests that click both.
 */
@Composable
private fun MissionSortToggle(
    sort: HistorySort,
    onSortChange: (HistorySort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(HistorySort.NEWEST, HistorySort.LARGEST)
    val labels = mapOf(
        HistorySort.NEWEST to stringResource(com.telegramdrive.uploader.feature.R.string.newest),
        HistorySort.LARGEST to stringResource(com.telegramdrive.uploader.feature.R.string.largest),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DesignTokens.AppColors.surfaceCard)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val selected = option == sort
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selected) {
                            DesignTokens.AppColors.purple.copy(alpha = 0.22f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onSortChange(option) }
                    .padding(vertical = 10.dp)
                    .testTag("sort_${option.name.lowercase()}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DesignTokens.AppColors.purpleHot,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = labels.getValue(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        DesignTokens.AppColors.purpleHot
                    } else {
                        DesignTokens.AppColors.contentMuted
                    },
                )
            }
        }
    }
}

@Composable
private fun periodLabel(period: HistoryPeriod): String = when (period) {
    HistoryPeriod.ALL -> stringResource(com.telegramdrive.uploader.feature.R.string.history_period_all)
    HistoryPeriod.TODAY -> stringResource(com.telegramdrive.uploader.feature.R.string.history_period_today)
    HistoryPeriod.LAST_7_DAYS -> stringResource(com.telegramdrive.uploader.feature.R.string.history_period_7_days)
    HistoryPeriod.LAST_30_DAYS -> stringResource(com.telegramdrive.uploader.feature.R.string.history_period_30_days)
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
