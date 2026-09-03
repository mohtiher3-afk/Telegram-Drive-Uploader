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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.core.ui.theme.AppMotion
import com.telegramdrive.uploader.core.ui.theme.rememberSystemMotionEnabled
import com.telegramdrive.uploader.core.ui.theme.SafeGlowTokens

@Composable
fun RealUploadProgressGlow(
    progressFraction: Float,
    statusColor: Color,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val signalCenter = Offset(
            x = size.width * progressFraction,
            y = size.height / 2f
        )
        drawCircle(
            color = statusColor.copy(alpha = SafeGlowTokens.HeroGlowColor * pulseAlpha),
            radius = size.height * (0.58f + (0.20f * pulseAlpha)),
            center = signalCenter
        )
        drawCircle(
            color = statusColor.copy(alpha = (SafeGlowTokens.HeroGlowColor * 2.5f) + (0.30f * pulseAlpha)),
            radius = size.height * 0.18f,
            center = signalCenter
        )
    }
}

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
                        RealUploadProgressGlow(
                            progressFraction = animatedProgressFraction,
                            statusColor = animatedStatusColor,
                            pulseAlpha = progressSignalPulse,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
                if (video.status == UploadStatus.UPLOADING) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TransferMetrics(
                        speed = video.speed,
                        eta = video.eta,
                        progressFraction = progressFraction,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Detailed interactive Action Log Section
            var isLogExpanded by remember { mutableStateOf(false) }
            val clipboardManager = LocalClipboardManager.current

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isLogExpanded = !isLogExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(com.telegramdrive.uploader.R.string.view_detailed_log),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isLogExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isLogExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val steps = listOf("ENQUEUED", "COMPRESSING", "CONNECTING", "UPLOADING", "VERIFYING", "COMPLETED")
                    val currentStepIdx = when (video.status) {
                        UploadStatus.QUEUED -> 0
                        UploadStatus.PREPARING -> 1
                        UploadStatus.UPLOADING -> 3
                        UploadStatus.COMPLETED -> 5
                        UploadStatus.FAILED -> 3
                        UploadStatus.CANCELLED -> 3
                        UploadStatus.PAUSED -> 3
                        UploadStatus.RETRYING -> 3
                    }

                    steps.forEachIndexed { idx, stepName ->
                        val isDone = idx < currentStepIdx || (video.status == UploadStatus.COMPLETED && idx == 5)
                        val isCurrent = idx == currentStepIdx && video.status != UploadStatus.COMPLETED
                        val dotColor = when {
                            isDone -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            androidx.compose.foundation.shape.CircleShape
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = dotColor
                            ) {}
                            Text(
                                text = stepName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDone || isCurrent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            if (video.status == UploadStatus.COMPLETED) {
                val mirrorLink = video.messageLink
                if (mirrorLink != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(com.telegramdrive.uploader.R.string.mirror_link_title),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = mirrorLink,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(mirrorLink)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(com.telegramdrive.uploader.R.string.copy_link),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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
