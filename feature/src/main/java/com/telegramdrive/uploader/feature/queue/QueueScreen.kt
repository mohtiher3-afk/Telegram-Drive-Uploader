package com.telegramdrive.uploader.feature.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.animation.AnimatedRecentUploadItem
import com.telegramdrive.uploader.core.ui.components.AppCard
import com.telegramdrive.uploader.core.ui.components.AppSearchField
import com.telegramdrive.uploader.core.ui.components.AppSegmentedTabs
import com.telegramdrive.uploader.core.ui.components.DotProgressBar
import com.telegramdrive.uploader.core.ui.components.DotProgressRing
import com.telegramdrive.uploader.core.ui.components.SectionHeader
import com.telegramdrive.uploader.core.ui.components.UploadStatusIndicator
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.feature.R

/**
 * Queue — the design package's screen 06 (`screens/06-queue-*.png`).
 *
 * Layout order per the prototype (`drive-uploader-ui.html` §queue):
 *  1. `(06) QUEUE` eyebrow, a 50sp Doto "Queue" title and the in-flight count
 *     on the trailing edge
 *  2. segmented tabs over the four real filters (the design draws three; the
 *     fourth — Paused — is a real state the ViewModel exposes and the tests pin)
 *  3. the active-transfer hero: the 72-dot ring, only while something uploads
 *  4. one card per queued item: file name + percent, a 26-dot bar, and the
 *     destination + size line
 *  5. batch controls: retry all failed / pause all active
 *
 * Presentation only: `QueueViewModel`, its filter and query state, and every
 * callback are unchanged. The dot bars carry the task's own progress; nothing
 * here synthesises a value.
 */
@Composable
fun QueueScreen(
    viewModel: QueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.queueItems.isEmpty() &&
            uiState.selectedFilter == QueueFilter.ALL &&
            uiState.query.isBlank()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                QueueHeader(uiState)
                // The search field and the filter tabs render above the empty state
                // rather than only inside the populated list: hiding them made the
                // filters unreachable on a fresh install.
                SearchAndFilters(
                    query = uiState.query,
                    onQueryChange = viewModel::onQueryChanged,
                    selectedFilter = uiState.selectedFilter,
                    onSelectFilter = viewModel::selectFilter
                )
                com.telegramdrive.uploader.core.ui.components.EmptyState(
                    icon = Icons.Default.HourglassEmpty,
                    title = stringResource(R.string.queue_empty_title),
                    supportingText = stringResource(R.string.queue_empty_supporting),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.phoneSection)
                        .testTag("queue_empty_state")
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("queue_list"),
                contentPadding = PaddingValues(bottom = AppSpacing.extraLarge * 2),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                item(key = "header") { QueueHeader(uiState) }

                item(key = "search") {
                    SearchAndFilters(
                        query = uiState.query,
                        onQueryChange = viewModel::onQueryChanged,
                        selectedFilter = uiState.selectedFilter,
                        onSelectFilter = viewModel::selectFilter
                    )
                }

                if (uiState.failedCount > 0 || uiState.activeCount > 0) {
                    item(key = "controls") {
                        QueueBatchControls(
                            failedCount = uiState.failedCount,
                            activeCount = uiState.activeCount,
                            onRetryAllFailed = viewModel::retryAllFailed,
                            onPauseAllActive = viewModel::pauseAllActive
                        )
                    }
                }

                // Active-upload hero: the 72-dot ring, only while something uploads.
                val activeUpload = uiState.queueItems.firstOrNull {
                    it.status == UploadStatus.UPLOADING
                }
                if (activeUpload != null) {
                    item(key = "active_ring") {
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("active_upload_ring")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                            ) {
                                Text(
                                    text = activeUpload.fileName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceMonoFamily
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                DotProgressRing(
                                    progress = activeUpload.progress / 100f,
                                    size = 224.dp,
                                    contentDescriptionText = "${activeUpload.progress.toInt()}%"
                                )
                            }
                        }
                    }
                }

                if (uiState.queueItems.isEmpty()) {
                    item(key = "filtered_empty") {
                        com.telegramdrive.uploader.core.ui.components.EmptyState(
                            icon = Icons.Default.HourglassEmpty,
                            title = stringResource(R.string.queue_no_matching_title),
                            supportingText = stringResource(R.string.queue_no_matching_supporting),
                            modifier = Modifier.testTag("queue_filtered_empty_state")
                        )
                    }
                } else {
                    // The queue is backed by a Room Flow, so once a frame is emitted the
                    // list is authoritative; there is no separate "loading" phase.
                    items(uiState.queueItems, key = { it.id }) { video ->
                        AnimatedRecentUploadItem(
                            fileName = video.fileName,
                            sizeLabel = "",
                            completed = false,
                            index = uiState.queueItems.indexOf(video)
                        ) {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacing.xSmall),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(AppSpacing.medium)) {
                                    VideoItem(
                                        video = video,
                                        onRemoveClick = { viewModel.removeUpload(video.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    UploadStatusIndicator(
                                        video = video,
                                        modifier = Modifier
                                            .padding(top = AppSpacing.xSmall)
                                            .fillMaxWidth(),
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

                // Either a non-blank search or a non-ALL filter can empty the list, and
                // both need the same explanation.
                if (uiState.queueItems.isEmpty() &&
                    (uiState.query.isNotBlank() || uiState.selectedFilter != QueueFilter.ALL)
                ) {
                    item(key = "no_results") {
                        Text(
                            text = stringResource(
                                if (uiState.query.isNotBlank()) {
                                    R.string.queue_no_matching_title
                                } else {
                                    R.string.queue_filtered_empty_title
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AppSpacing.medium)
                                .testTag("queue_no_results"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item(key = "tail") { Spacer(modifier = Modifier.height(AppSpacing.medium)) }
            }
        }
    }
}

/** Batch controls: retry all failed / pause all active, shown only when relevant. */
@Composable
private fun QueueBatchControls(
    failedCount: Int,
    activeCount: Int,
    onRetryAllFailed: () -> Unit,
    onPauseAllActive: () -> Unit
) {
    if (failedCount <= 0 && activeCount <= 0) return
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Text(
                text = stringResource(R.string.queue_controls_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.queue_controls_supporting),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.xSmall),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                if (failedCount > 0) {
                    FilledTonalButton(
                        onClick = onRetryAllFailed,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = AppSpacing.touchTarget)
                            .testTag("retry_all_failed"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.small))
                        Text(
                            text = stringResource(R.string.retry),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                if (activeCount > 0) {
                    FilledTonalButton(
                        onClick = onPauseAllActive,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = AppSpacing.touchTarget)
                            .testTag("pause_all_active"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.PauseCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.small))
                        Text(
                            text = stringResource(R.string.pause),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/** `(06) QUEUE` eyebrow, the Doto title, the count, and the summary line. */
@Composable
private fun QueueHeader(uiState: QueueUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.medium, bottom = AppSpacing.small),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionHeader(
                number = "06",
                label = "QUEUE",
                title = stringResource(R.string.upload_queue),
                counter = uiState.activeCount.toString().padStart(2, '0'),
                subtitle = pluralStringResource(
                    R.plurals.queue_count_summary,
                    uiState.activeCount,
                    uiState.activeCount,
                    uiState.failedCount
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Search field plus the filter tabs.
 *
 * Kept as one composable because both the empty and the populated branch render
 * it, and a copy that drifts between the two is exactly the difference no test
 * notices. The `phoneEdge` padding is applied only by the empty-state caller; the
 * list branch already pads its own content.
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
        AppSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.filter_active_queue),
            modifier = Modifier.fillMaxWidth(),
            textFieldModifier = Modifier.testTag("queue_search_field")
        )
        Spacer(modifier = Modifier.height(AppSpacing.xSmall))
        AppSegmentedTabs(
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
    QueueFilter.ALL -> stringResource(R.string.queue_filter_all)
    QueueFilter.ACTIVE -> stringResource(R.string.queue_filter_active)
    QueueFilter.PAUSED -> stringResource(R.string.queue_filter_paused)
    QueueFilter.FAILED -> stringResource(R.string.queue_filter_failed)
}
