package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DotoFamily
import com.telegramdrive.uploader.core.ui.theme.SpaceMonoFamily
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.feature.R

/**
 * Island notification - the floating progress pill at the top of the screen.
 * Matches the prototype's `.isl` component with compact/expanded/done/failed states.
 */
@Composable
fun UploadIsland(
    activeUpload: UploadTask?,
    isVisible: Boolean,
    onExpandClick: () -> Unit,
    onPauseClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!isVisible || activeUpload == null) return

    val progressPercent = (activeUpload.progress.coerceIn(0f, 100f)).toInt()
    val speedText = if (activeUpload.speed > 0) {
        "${formatSpeed(activeUpload.speed)}"
    } else {
        stringResource(R.string.initializing_relay)
    }

    var isExpanded by remember { mutableStateOf(false) }

    val state = when {
        activeUpload.status == UploadStatus.COMPLETED -> IslandState.Done
        activeUpload.status == UploadStatus.FAILED -> IslandState.Failed
        isExpanded -> IslandState.Expanded
        else -> IslandState.Compact
    }

    // Pulse animation for active uploads - hoisted out of graphicsLayer
    val pulseAlpha by animateFloatAsState(
        targetValue = if (activeUpload.status == UploadStatus.UPLOADING ||
            activeUpload.status == UploadStatus.PREPARING) 0.5f else 1f,
        animationSpec = AppMotion.pulse(),
        label = "island_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.medium)
            .animateContentSize(animationSpec = AppMotion.mediumSpring())
            .testTag("island_notification")
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = 1f
                },
            shape = when (state) {
                IslandState.Compact -> RoundedCornerShape(14.dp)
                IslandState.Expanded -> RoundedCornerShape(30.dp)
                IslandState.Done, IslandState.Failed -> RoundedCornerShape(30.dp)
            },
            colors = CardDefaults.cardColors(
                containerColor = when (state) {
                    IslandState.Compact -> MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    IslandState.Expanded -> MaterialTheme.colorScheme.surface
                    IslandState.Done -> MaterialTheme.colorScheme.surface
                    IslandState.Failed -> MaterialTheme.colorScheme.errorContainer
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded; onExpandClick() }
                    .padding(horizontal = 16.dp, vertical = when (state) {
                        IslandState.Compact -> 6.dp
                        IslandState.Expanded -> 16.dp
                        IslandState.Done, IslandState.Failed -> 16.dp
                    })
                    .animateContentSize(animationSpec = AppMotion.mediumSpring()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Compact row: dot + percent + speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pulsing dot
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .graphicsLayer {
                                    alpha = pulseAlpha
                                }
                        ) {}

                        // Percent
                        Text(
                            text = when (state) {
                                IslandState.Done -> stringResource(R.string.island_done)
                                IslandState.Failed -> stringResource(R.string.island_failed)
                                else -> "$progressPercent%"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = DotoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Speed (compact)
                    if (state == IslandState.Compact) {
                        Text(
                            text = speedText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expanded content
                if (state == IslandState.Expanded || state == IslandState.Done || state == IslandState.Failed) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // File name
                    Text(
                        text = activeUpload.fileName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = DotoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    // Dot progress bar
                    Spacer(modifier = Modifier.height(10.dp))
                    DotProgressBar(
                        progress = activeUpload.progress / 100f,
                        contentDescriptionText = stringResource(R.string.upload_progress_accessibility, progressPercent),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Expanded footer: speed + pause/cancel
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeUpload.status == UploadStatus.UPLOADING || activeUpload.status == UploadStatus.PREPARING) {
                                "↑ $speedText"
                            } else if (state == IslandState.Done) {
                                stringResource(R.string.island_sent)
                            } else if (state == IslandState.Failed) {
                                stringResource(R.string.island_failed_short)
                            } else {
                                stringResource(R.string.island_paused)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontSize = 10.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (state == IslandState.Expanded) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Pause/Resume
                                if (activeUpload.status == UploadStatus.UPLOADING || activeUpload.status == UploadStatus.PREPARING) {
                                    onPauseClick?.let { pauseClick ->
                                        IconButton(
                                            onClick = pauseClick,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pause,
                                                contentDescription = stringResource(R.string.pause),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                } else {
                                    onPauseClick?.let { resumeClick ->
                                        IconButton(
                                            onClick = resumeClick,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = stringResource(R.string.resume),
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                // Cancel
                                onCancelClick?.let { cancelClick ->
                                    IconButton(
                                        onClick = cancelClick,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class IslandState {
    Compact,
    Expanded,
    Done,
    Failed
}

private fun formatSpeed(bytesPerSecond: Long): String {
    return when {
        bytesPerSecond >= 1_000_000_000 -> "${(bytesPerSecond / 1_000_000_000.0).toString().take(4)} GB/s"
        bytesPerSecond >= 1_000_000 -> "${(bytesPerSecond / 1_000_000.0).toString().take(4)} MB/s"
        bytesPerSecond >= 1_000 -> "${(bytesPerSecond / 1_000.0).toString().take(4)} KB/s"
        else -> "$bytesPerSecond B/s"
    }
}