package com.telegramdrive.uploader.feature.upload

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.text.DateFormat
import java.util.Calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.core.ui.components.EmptyState
import com.telegramdrive.uploader.core.ui.components.ErrorState
import com.telegramdrive.uploader.core.ui.components.GradientButton
import com.telegramdrive.uploader.core.ui.components.GradientCard
import com.telegramdrive.uploader.core.ui.components.VideoItem
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.GradientPalette
import com.telegramdrive.uploader.core.ui.theme.GradientPalettes
import com.telegramdrive.uploader.core.util.media.VideoQualityPreset

/** Calm, near-slate gradient for low-emphasis tiles. Matches Home's CalmSlate. */
private val CalmSlate = GradientPalette(
    top = Color(0xFF23262E),
    mid = Color(0xFF1E2128),
    base = Color(0xFF181B21),
    glow = Color(0xFF2A2E38)
)

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
    val scheduledAt by viewModel.scheduledAt.collectAsStateWithLifecycle()
    val smartSuggestions by viewModel.smartSuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedVideoIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedVideoIds.isNotEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isSelectionMode && uiState is UploadUiState.Success) {
                val successState = uiState as UploadUiState.Success
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(com.telegramdrive.uploader.R.string.selected) + " ${selectedVideoIds.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { selectedVideoIds = emptySet() },
                            modifier = Modifier.testTag("selection_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(com.telegramdrive.uploader.R.string.clear_selection)
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                selectedVideoIds = successState.preparedVideos.map { it.id }.toSet()
                            },
                            modifier = Modifier.testTag("selection_select_all_button")
                        ) {
                            Text(
                                stringResource(com.telegramdrive.uploader.R.string.select_all),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = {
                                val videosToRemove = successState.preparedVideos.filter { it.id in selectedVideoIds }
                                viewModel.removePreparedVideos(videosToRemove)
                                selectedVideoIds = emptySet()
                            },
                            modifier = Modifier.testTag("selection_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(com.telegramdrive.uploader.R.string.remove_selected),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(com.telegramdrive.uploader.R.string.prepare_videos)) },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("upload_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(com.telegramdrive.uploader.R.string.back)
                            )
                        }
                    }
                )
            }
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
                        title = stringResource(com.telegramdrive.uploader.R.string.no_videos_selected),
                        supportingText = stringResource(com.telegramdrive.uploader.R.string.select_videos_from_home),
                        actionText = stringResource(com.telegramdrive.uploader.R.string.go_back),
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
                            text = stringResource(com.telegramdrive.uploader.R.string.extracting_video_metadata),
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
                            title = stringResource(com.telegramdrive.uploader.R.string.all_videos_removed),
                            supportingText = stringResource(com.telegramdrive.uploader.R.string.select_more_videos),
                            actionText = stringResource(com.telegramdrive.uploader.R.string.go_back),
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
                            GradientCard(
                                palette = if (selectedDestination != null) {
                                    GradientPalettes.Ocean
                                } else {
                                    CalmSlate
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.telegram_destination_label),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selectedDestination != null)
                                                GradientPalettes.Ocean.content.copy(alpha = 0.85f)
                                            else
                                                CalmSlate.content.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = selectedDestination?.title ?: stringResource(com.telegramdrive.uploader.R.string.select_target),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = if (selectedDestination != null)
                                                GradientPalettes.Ocean.content
                                            else
                                                CalmSlate.content
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onSelectDestination,
                                        shape = RoundedCornerShape(28.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = if (selectedDestination != null)
                                                GradientPalettes.Ocean.content
                                            else
                                                CalmSlate.content
                                        )
                                    ) {
                                        Text(stringResource(if (selectedDestination == null) com.telegramdrive.uploader.R.string.select_action else com.telegramdrive.uploader.R.string.schedule_change))
                                    }
                                }
                            }

                            GradientCard(
                                palette = CalmSlate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            stringResource(com.telegramdrive.uploader.R.string.schedule_upload),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = CalmSlate.content
                                        )
                                        Text(
                                            scheduledAt?.let {
                                                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(it)
                                            } ?: stringResource(com.telegramdrive.uploader.R.string.start_immediately),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = CalmSlate.content.copy(alpha = 0.82f)
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            val calendar = Calendar.getInstance()
                                            DatePickerDialog(
                                                context,
                                                { _, year, month, day ->
                                                    calendar.set(Calendar.YEAR, year)
                                                    calendar.set(Calendar.MONTH, month)
                                                    calendar.set(Calendar.DAY_OF_MONTH, day)
                                                    TimePickerDialog(
                                                        context,
                                                        { _, hour, minute ->
                                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                            calendar.set(Calendar.MINUTE, minute)
                                                            calendar.set(Calendar.SECOND, 0)
                                                            calendar.set(Calendar.MILLISECOND, 0)
                                                            viewModel.setScheduledAt(calendar.timeInMillis)
                                                        },
                                                        calendar.get(Calendar.HOUR_OF_DAY),
                                                        calendar.get(Calendar.MINUTE),
                                                        true
                                                    ).show()
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = CalmSlate.content
                                        ),
                                        modifier = Modifier.testTag("schedule_upload_button")
                                    ) { Text(stringResource(if (scheduledAt == null) com.telegramdrive.uploader.R.string.schedule_choose else com.telegramdrive.uploader.R.string.schedule_change)) }
                                    if (scheduledAt != null) {
                                        TextButton(
                                            onClick = { viewModel.setScheduledAt(null) },
                                            colors = ButtonDefaults.textButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = CalmSlate.content
                                            )
                                        ) { Text(stringResource(com.telegramdrive.uploader.R.string.clear)) }
                                    }
                                }
                            }

                            // Smart File Assistant
                            GradientCard(
                                palette = GradientPalettes.Neon,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_title),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = GradientPalettes.Neon.content
                                        )
                                        Text(
                                            text = stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_description),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GradientPalettes.Neon.content.copy(alpha = 0.82f)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = viewModel::applyAllSmartSuggestions,
                                        enabled = smartSuggestions.isNotEmpty(),
                                        shape = MaterialTheme.shapes.extraLarge,
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = GradientPalettes.Neon.content
                                        ),
                                        modifier = Modifier.testTag("smart_file_assistant_button")
                                    ) {
                                        Text(stringResource(com.telegramdrive.uploader.R.string.smart_file_assistant_suggest))
                                    }
                                }
                            }
                            // Warning Banner if invalid files were skipped
                            state.invalidFilesWarning?.let { warning ->
                                GradientCard(
                                    palette = GradientPalettes.Sunset,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    testTag = "invalid_files_warning_card"
                                ) {
                                    Text(
                                        text = warning,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GradientPalettes.Sunset.content
                                    )
                                }
                            }

                            // Compression Quality Selector
                            GradientCard(
                                palette = CalmSlate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                shape = RoundedCornerShape(24.dp),
                                testTag = "compression_selector_card"
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(com.telegramdrive.uploader.R.string.compression_title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CalmSlate.content
                                            )
                                            Text(
                                                text = stringResource(com.telegramdrive.uploader.R.string.compression_subtitle),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CalmSlate.content.copy(alpha = 0.82f)
                                            )
                                        }
                                        if (state.isCompressing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = CalmSlate.content
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        VideoQualityPreset.values().forEach { preset ->
                                            FilterChip(
                                                selected = state.compressionPreset == preset,
                                                onClick = { viewModel.setCompressionPreset(preset) },
                                                enabled = !state.isCompressing,
                                                label = {
                                                    Text(
                                                        text = when (preset) {
                                                            VideoQualityPreset.LOW -> stringResource(com.telegramdrive.uploader.R.string.compression_low)
                                                            VideoQualityPreset.MEDIUM -> stringResource(com.telegramdrive.uploader.R.string.compression_medium)
                                                            VideoQualityPreset.HIGH -> stringResource(com.telegramdrive.uploader.R.string.compression_high)
                                                            VideoQualityPreset.ORIGINAL -> stringResource(com.telegramdrive.uploader.R.string.compression_original)
                                                        }
                                                    )
                                                },
                                                modifier = Modifier.testTag("compression_${preset.name.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }

                            // Summary Header
                            GradientCard(
                                palette = GradientPalettes.Neon,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.videos_selected_summary, state.preparedVideos.size),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GradientPalettes.Neon.content
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.total_size_summary, formatFileSize(totalSize)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GradientPalettes.Neon.content.copy(alpha = 0.85f)
                                )
                            }

                            // List
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("prepared_video_list"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.preparedVideos, key = { it.id }) { video ->
                                    val isSelected = video.id in selectedVideoIds
                                    VideoItem(
                                        video = video,
                                        isSelected = isSelected,
                                        onSelectedChange = { checked ->
                                            selectedVideoIds = if (checked) {
                                                selectedVideoIds + video.id
                                            } else {
                                                selectedVideoIds - video.id
                                            }
                                        },
                                        onRemoveClick = { viewModel.removePreparedVideo(video) },
                                        modifier = Modifier.clickable {
                                            selectedVideoIds = if (isSelected) {
                                                selectedVideoIds - video.id
                                            } else {
                                                selectedVideoIds + video.id
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bottom Add to Queue Action
                            GradientButton(
                                text = stringResource(com.telegramdrive.uploader.R.string.add_to_queue),
                                onClick = { viewModel.addToQueue(onComplete = onQueueAdded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .testTag("add_to_queue_button"),
                                palette = GradientPalettes.Neon,
                                shape = RoundedCornerShape(28.dp),
                                enabled = selectedDestination != null && !state.isSubmitting,
                                loading = state.isSubmitting
                            )
                        }
                    }
                }
            }
        }
    }
}
