package com.telegramdrive.uploader.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask

/**
 * Renders active uploads as Telegram-style chat bubbles.
 * Each bubble shows the file name, live status/progress and quick
 * retry / cancel actions where applicable.
 */
@Composable
fun UploadsChatBubbleList(
    uploads: List<UploadTask>,
    onRetryClicked: (String) -> Unit,
    onCancelClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uploads.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.sm)
    ) {
        uploads.forEach { upload ->
            UploadChatBubble(
                upload = upload,
                onRetryClicked = { onRetryClicked(upload.id) },
                onCancelClicked = { onCancelClicked(upload.id) }
            )
        }
    }
}

@Composable
private fun UploadChatBubble(
    upload: UploadTask,
    onRetryClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    val isActive = upload.status == UploadStatus.PREPARING ||
        upload.status == UploadStatus.UPLOADING ||
        upload.status == UploadStatus.RETRYING
    val canCancel = upload.status == UploadStatus.QUEUED || isActive
    val canRetry = upload.status == UploadStatus.FAILED ||
        upload.status == UploadStatus.CANCELLED

    Surface(
        modifier = Modifier.widthIn(max = 340.dp),
        shape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 18.dp,
            bottomEnd = 18.dp,
            bottomStart = 18.dp
        ),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        contentColor = MaterialTheme.colorScheme.onSecondary
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.AppSpacing.xs)
        ) {
            Text(
                text = upload.fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(statusLabel(upload.status))
                    if (isActive) {
                        append(" \u2022 ")
                        append((upload.progress * 100f).toInt().coerceIn(0, 100))
                        append("%")
                    }
                    append(" \u2022 ")
                    append(formatFileSize(upload.uploadedBytes))
                    append(" / ")
                    append(formatFileSize(upload.fileSize))
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
            )
            if (upload.status == UploadStatus.FAILED) {
                val err = upload.lastError
                if (err != null) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (canRetry || canCancel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canRetry) {
                        IconButton(
                            onClick = onRetryClicked,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry upload",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (canCancel) {
                        IconButton(
                            onClick = onCancelClicked,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel upload",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun statusLabel(status: UploadStatus): String = when (status) {
    UploadStatus.QUEUED -> "Queued"
    UploadStatus.PREPARING -> "Preparing"
    UploadStatus.UPLOADING -> "Uploading"
    UploadStatus.PAUSED -> "Paused"
    UploadStatus.RETRYING -> "Retrying"
    UploadStatus.COMPLETED -> "Completed"
    UploadStatus.FAILED -> "Failed"
    UploadStatus.CANCELLED -> "Cancelled"
}
