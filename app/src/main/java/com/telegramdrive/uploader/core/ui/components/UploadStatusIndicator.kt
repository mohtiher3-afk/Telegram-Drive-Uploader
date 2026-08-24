package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask

internal fun uploadProgressFraction(percentage: Float): Float =
    (percentage / 100f).coerceIn(0f, 1f)

internal fun uploadProgressPercent(percentage: Float): Int =
    percentage.coerceIn(0f, 100f).toInt()

@Composable
fun UploadStatusIndicator(
    video: UploadTask,
    modifier: Modifier = Modifier,
    onPauseClick: (() -> Unit)? = null,
    onResumeClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${video.status.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = when (video.status) {
                        UploadStatus.FAILED -> MaterialTheme.colorScheme.error
                        UploadStatus.UPLOADING -> MaterialTheme.colorScheme.primary
                        UploadStatus.PAUSED -> MaterialTheme.colorScheme.outline
                        UploadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (video.progress > 0 && video.status != UploadStatus.COMPLETED) {
                    Text(
                        text = "${uploadProgressPercent(video.progress)}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            if (video.status == UploadStatus.UPLOADING || video.status == UploadStatus.PREPARING) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { uploadProgressFraction(video.progress) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (video.status == UploadStatus.UPLOADING) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(
                                com.telegramdrive.uploader.R.string.upload_speed,
                                formatTransferSpeed(video.speed)
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val etaText = formatRemainingTime(video.eta)
                        val etaLabel = when {
                            etaText.isNotEmpty() -> etaText
                            video.speed > 0L -> stringResource(com.telegramdrive.uploader.R.string.upload_eta_calculating)
                            else -> stringResource(com.telegramdrive.uploader.R.string.upload_eta_stalled)
                        }
                        Text(
                            text = stringResource(
                                com.telegramdrive.uploader.R.string.upload_eta,
                                etaLabel
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (onPauseClick != null || onResumeClick != null || onRetryClick != null || onCancelClick != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (video.status) {
                        UploadStatus.UPLOADING,
                        UploadStatus.QUEUED,
                        UploadStatus.RETRYING -> {
                            onPauseClick?.let {
                                TextButton(onClick = it) {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.pause))
                                }
                            }
                        }
                        UploadStatus.PAUSED -> {
                            onResumeClick?.let {
                                TextButton(onClick = it) {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.resume))
                                }
                            }
                        }
                        UploadStatus.FAILED -> {
                            onRetryClick?.let {
                                TextButton(onClick = it) {
                                    Text(stringResource(com.telegramdrive.uploader.R.string.retry))
                                }
                            }
                        }
                        else -> {}
                    }

                    if (video.status != UploadStatus.CANCELLED && video.status != UploadStatus.COMPLETED) {
                        onCancelClick?.let {
                            TextButton(
                                onClick = it,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(com.telegramdrive.uploader.R.string.cancel))
                            }
                        }
                    }
                }
            }
        }
    }
}
