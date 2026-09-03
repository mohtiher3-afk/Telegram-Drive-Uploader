package com.telegramdrive.uploader.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.R

/**
 * A beautiful, Material 3 component designed to visualize upload speeds
 * and estimated time remaining (ETA) inside transfer list items using progress indicators.
 */
@Composable
fun TransferMetrics(
    speed: Long,
    eta: Long,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    // Map speed to progress value between 0.0 and 1.0 (e.g., maxing out visual gauge at 10 MB/s)
    val maxTrackedSpeed = 10_485_760L // 10 MB/s
    val speedRatio = (speed.toFloat() / maxTrackedSpeed).coerceIn(0f, 1f)
    
    val animatedSpeedProgress by animateFloatAsState(
        targetValue = speedRatio,
        label = "transfer_speed_progress"
    )

    val animatedEtaProgress by animateFloatAsState(
        targetValue = (1f - progressFraction).coerceIn(0f, 1f),
        label = "transfer_eta_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transfer_metrics_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Metrics Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag("speed_metrics_col"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = stringResource(R.string.upload_speed_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(
                            R.string.upload_speed,
                            formatTransferSpeed(speed)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Speed gauge visualizer
                LinearProgressIndicator(
                    progress = { animatedSpeedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            }

            // ETA / Time Remaining Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .testTag("eta_metrics_col"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = stringResource(R.string.time_remaining_desc),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )

                    val etaText = formatRemainingTime(eta)
                    val etaLabel = when {
                        etaText.isNotEmpty() -> etaText
                        speed > 0L -> stringResource(R.string.upload_eta_calculating)
                        else -> stringResource(R.string.upload_eta_stalled)
                    }

                    Text(
                        text = stringResource(R.string.upload_eta, etaLabel),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Time remaining visualizer (displays percentage of remaining transfer)
                LinearProgressIndicator(
                    progress = { animatedEtaProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                )
            }
        }
    }
}
