package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.telegramdrive.uploader.core.ui.theme.AppRadius
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.feature.R
import java.io.File
import java.util.Locale

/**
 * Prepared-video row used by the upload preparation screen (05).
 * Rebuilt on Dusk: plain AppCard.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VideoItem(
    video: UploadTask,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_item_${video.id}"),
        containerColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        }
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.small)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onSelectedChange != null) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectedChange,
                    modifier = Modifier
                        .padding(end = AppSpacing.xSmall)
                        .testTag("video_item_checkbox_${video.id}")
                )
            }

            // Thumbnail / Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(AppRadius.control))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val thumbnailFile = video.thumbnailPath?.let(::File)?.takeIf(File::exists)
                if (thumbnailFile != null) {
                    AsyncImage(
                        model = thumbnailFile,
                        contentDescription = stringResource(R.string.video_thumbnail),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = stringResource(R.string.video_icon),
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.small + AppSpacing.xSmall))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetadataPill(text = formatFileSize(video.fileSize))
                    MetadataPill(text = formatDuration(video.duration))
                    if (video.width > 0 && video.height > 0) {
                        MetadataPill(text = "${video.width} × ${video.height}")
                    }
                }
            }

            if (onRemoveClick != null) {
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.testTag("remove_video_button_${video.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_video),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(AppRadius.chip)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}