package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled

internal fun uploadProgressFraction(percentage: Float): Float =
    (percentage / 100f).coerceIn(0f, 1f)

internal fun uploadProgressPercent(percentage: Float): Int =
    percentage.coerceIn(0f, 100f).toInt()

internal fun uploadStatusLabelRes(status: UploadStatus): Int = when (status) {
    UploadStatus.QUEUED -> com.telegramdrive.uploader.R.string.status_queued
    UploadStatus.PREPARING -> com.telegramdrive.uploader.R.string.status_preparing
    UploadStatus.UPLOADING -> com.telegramdrive.uploader.R.string.status_uploading
    UploadStatus.PAUSED -> com.telegramdrive.uploader.R.string.status_paused
    UploadStatus.RETRYING -> com.telegramdrive.uploader.R.string.status_retrying
    UploadStatus.COMPLETED -> com.telegramdrive.uploader.R.string.status_completed
    UploadStatus.FAILED -> com.telegramdrive.uploader.R.string.status_failed
    UploadStatus.CANCELLED -> com.telegramdrive.uploader.R.string.status_cancelled
}

@Composable
fun UploadStatusIndicator(
    video: UploadTask,
    modifier: Modifier = Modifier,
    onPauseClick: (() -> Unit)? = null,
    onResumeClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null
) {
    val motionEnabled = rememberSystemMotionEnabled()
    val progressFraction = uploadProgressFraction(video.progress)
    val animatedProgressFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "upload_progress"
    )
    val progressPercent = uploadProgressPercent(video.progress)
    val activeUpload = video.status == UploadStatus.UPLOADING && progressFraction < 1f
    val progressSignalPulse = if (motionEnabled && activeUpload) {
        rememberInfiniteTransition(label = "upload_progress_signal")
            .animateFloat(
                initialValue = 0.55f,
                targetValue = 1f,
                animationSpec = AppMotion.uploadSignalPulse(),
                label = "upload_progress_signal_alpha"
            )
            .value
    } else {
        0f
    }
    val statusLabel = stringResource(uploadStatusLabelRes(video.status))
    val targetStatusColor = when (video.status) {
        UploadStatus.FAILED -> MaterialTheme.colorScheme.error
        UploadStatus.UPLOADING -> MaterialTheme.colorScheme.primary
        UploadStatus.PREPARING -> MaterialTheme.colorScheme.secondary
        UploadStatus.PAUSED -> MaterialTheme.colorScheme.outline
        UploadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val animatedStatusColor by animateColorAsState(
        targetValue = targetStatusColor,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "upload_status_color"
    )
    val targetContainerColor = when (video.status) {
        UploadStatus.UPLOADING, UploadStatus.PREPARING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f)
        UploadStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.20f)
        UploadStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = AppMotion.shortTween(motionEnabled),
        label = "upload_status_container"
    )
    val progressDescription = stringResource(
        com.telegramdrive.uploader.R.string.upload_progress_accessibility,
        progressPercent
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = AppMotion.shortTween(motionEnabled)),
        shape = MaterialTheme.shapes.small,
        color = animatedContainerColor
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(
                    targetState = statusLabel,
                    animationSpec = AppMotion.shortTween(motionEnabled),
                    label = "upload_status_label"
                ) { visibleStatusLabel ->
                    Text(
                        text = stringResource(
                            com.telegramdrive.uploader.R.string.upload_status,
                            visibleStatusLabel
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = animatedStatusColor
                    )
                }

                if (video.progress > 0 && video.status != UploadStatus.COMPLETED) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            AnimatedVisibility(
                visible = video.status == UploadStatus.UPLOADING || video.status == UploadStatus.PREPARING,
                enter = if (motionEnabled) fadeIn(animationSpec = AppMotion.shortTween()) else EnterTransition.None,
                exit = if (motionEnabled) fadeOut(animationSpec = AppMotion.shortTween()) else ExitTransition.None
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgressFraction },
                        color = animatedStatusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .semantics {
                                progressBarRangeInfo = ProgressBarRangeInfo(
                                    current = progressFraction,
                                    range = 0f..1f,
                                    steps = 0
                                )
                                contentDescription = progressDescription
                            }
                    )
                    if (motionEnabled && activeUpload) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val signalCenter = Offset(
                                x = size.width * animatedProgressFraction,
                                y = size.height / 2f
                            )
                            drawCircle(
                                color = animatedStatusColor.copy(alpha = 0.12f * progressSignalPulse),
                                radius = size.height * (0.58f + (0.20f * progressSignalPulse)),
                                center = signalCenter
                            )
                            drawCircle(
                                color = animatedStatusColor.copy(alpha = 0.52f + (0.30f * progressSignalPulse)),
                                radius = size.height * 0.18f,
                                center = signalCenter
                            )
                        }
                    }
                }
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
