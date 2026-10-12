package com.telegramdrive.uploader.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.animation.AnimatedRecentUploadItem
import com.telegramdrive.uploader.core.ui.components.AppCard
import com.telegramdrive.uploader.core.ui.components.AppSearchField
import com.telegramdrive.uploader.core.ui.components.AppSegmentedTabs
import com.telegramdrive.uploader.core.ui.components.Eyebrow
import com.telegramdrive.uploader.core.ui.components.SectionHeader
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.feature.R
import java.util.Locale

/**
 * History — the design package's screen 06b (`screens/06b-queue-done-*.png`).
 *
 * The design draws history as the queue's "Done" tab: a section eyebrow, a Doto
 * title, the search field, the period tabs, a summary card, a newest/largest
 * toggle, then one card per delivered upload with its elapsed time.
 *
 * Presentation only: `HistoryViewModel`, its query/period/sort state and every
 * callback are unchanged. The summary reads the real match count and byte total
 * from the ViewModel; nothing here invents a figure.
 */
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.totalMatches == 0 && uiState.query.isBlank() && uiState.period == HistoryPeriod.ALL) {
            Column(modifier = Modifier.fillMaxSize()) {
                HistoryHeader(
                    totalMatches = uiState.totalMatches,
                    onClear = viewModel::clearHistory
                )
                // Same reasoning as the queue screen: the search box and the period
                // tabs belong to the screen, not to the list. Hiding them on an empty
                // history left the filters unreachable on a fresh install.
                SearchAndPeriods(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                    period = uiState.period,
                    onPeriodChange = viewModel::setPeriod,
                    modifier = Modifier.padding(horizontal = AppSpacing.phoneEdge)
                )
                com.telegramdrive.uploader.core.ui.components.EmptyState(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.history_no_uploads),
                    supportingText = stringResource(R.string.history_no_uploads_supporting),
                    modifier = Modifier
                        .padding(
                            horizontal = AppSpacing.phoneEdge,
                            vertical = AppSpacing.phoneSection
                        )
                        .testTag("history_empty_state")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.phoneEdge)
                    .testTag("history_list"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = AppSpacing.extraLarge * 2
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                item(key = "header") {
                    HistoryHeader(
                        totalMatches = uiState.totalMatches,
                        onClear = viewModel::clearHistory
                    )
                }
                item(key = "search") {
                    Spacer(modifier = Modifier.height(AppSpacing.xSmall))
                    SearchAndPeriods(
                        query = uiState.query,
                        onQueryChange = viewModel::onQueryChanged,
                        period = uiState.period,
                        onPeriodChange = viewModel::setPeriod
                    )
                }
                item(key = "banner") {
                    AppCard(modifier = Modifier.testTag("history_banner")) {
                        Column(modifier = Modifier.padding(AppSpacing.medium)) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.history_matches_summary,
                                    uiState.totalMatches,
                                    uiState.totalMatches,
                                    formatFileSize(uiState.totalSize)
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.history_banner_supporting),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item(key = "sort") {
                    HistorySortToggle(
                        sort = uiState.sort,
                        onSortChange = viewModel::setSort,
                        modifier = Modifier.testTag("history_sort")
                    )
                }
                if (uiState.historyItems.isEmpty()) {
                    item(key = "filtered_empty") {
                        com.telegramdrive.uploader.core.ui.components.EmptyState(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.history_no_matching),
                            supportingText = stringResource(R.string.history_no_matching_supporting),
                            modifier = Modifier.testTag("history_filtered_empty_state")
                        )
                    }
                } else {
                    items(uiState.historyItems, key = { it.id }) { video ->
                        AnimatedRecentUploadItem(
                            fileName = video.fileName,
                            sizeLabel = "",
                            completed = video.status == UploadStatus.COMPLETED,
                            index = uiState.historyItems.indexOf(video)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                VideoItem(
                                    video = video,
                                    onRemoveClick = { viewModel.deleteUpload(video.id) }
                                )
                                if (video.uploadDurationMs > 0L) {
                                    Text(
                                        text = stringResource(
                                            R.string.upload_time,
                                            formatElapsedUploadTime(video.uploadDurationMs)
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = AppSpacing.xSmall)
                                    )
                                }
                                UploadStatusIndicator(
                                    video = video,
                                    modifier = Modifier.padding(top = AppSpacing.xSmall)
                                )
                            }
                        }
                    }
                }
                item(key = "tail") { Spacer(modifier = Modifier.height(AppSpacing.medium)) }
            }
        }
    }
}

/** The `(06) HISTORY` header with the Doto title and the clear action. */
@Composable
private fun HistoryHeader(
    totalMatches: Int,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.medium, bottom = AppSpacing.small),
        verticalAlignment = Alignment.Top
    ) {
        SectionHeader(
            number = "06",
            label = "HISTORY",
            title = stringResource(R.string.upload_history),
            modifier = Modifier.weight(1f)
        )
        if (totalMatches > 0) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = stringResource(R.string.clear_history),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Search field plus the period tabs, shared by the empty and populated branches.
 *
 * Extracted so the two call sites cannot drift, which is the same reason the queue
 * screen has its own `SearchAndFilters`.
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
        AppSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.filter_completed_uploads),
            modifier = Modifier.fillMaxWidth(),
            textFieldModifier = Modifier.testTag("history_search_field")
        )
        Spacer(modifier = Modifier.height(AppSpacing.xSmall))
        AppSegmentedTabs(
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
 * Newest / Largest toggle, rendered as a two-segment control so it reads as one
 * choice rather than two independent chips. Selecting the active option is a
 * no-op rather than an error, which keeps the toggle usable by screen readers.
 */
@Composable
private fun HistorySortToggle(
    sort: HistorySort,
    onSortChange: (HistorySort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(HistorySort.NEWEST, HistorySort.LARGEST)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppSpacing.touchTarget / 4))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(AppSpacing.xSmall),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val selected = option == sort
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(AppSpacing.medium))
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onSortChange(option) }
                    .padding(vertical = AppSpacing.small + AppSpacing.xSmall)
                    .testTag("sort_${option.name.lowercase()}"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.small - AppSpacing.xSmall))
                }
                Text(
                    text = stringResource(
                        when (option) {
                            HistorySort.NEWEST -> R.string.newest
                            HistorySort.LARGEST -> R.string.largest
                        }
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun periodLabel(period: HistoryPeriod): String = when (period) {
    HistoryPeriod.ALL -> stringResource(R.string.history_period_all)
    HistoryPeriod.TODAY -> stringResource(R.string.history_period_today)
    HistoryPeriod.LAST_7_DAYS -> stringResource(R.string.history_period_7_days)
    HistoryPeriod.LAST_30_DAYS -> stringResource(R.string.history_period_30_days)
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
