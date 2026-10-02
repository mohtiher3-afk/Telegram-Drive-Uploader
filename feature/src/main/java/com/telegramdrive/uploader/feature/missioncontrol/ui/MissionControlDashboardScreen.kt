package com.telegramdrive.uploader.feature.missioncontrol.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.feature.missioncontrol.model.ActiveTransferItem
import com.telegramdrive.uploader.feature.missioncontrol.model.MissionControlUiState
import com.telegramdrive.uploader.feature.missioncontrol.theme.MissionControlTokens
import java.util.Locale
/**
 * Mission Control dashboard.
 *
 * Every value shown comes from [uiState]; this composable invents no telemetry. The
 * caller supplies a [MissionControlUiState] produced from real upload state.
 */
@Composable
fun MissionControlDashboardScreen(
    uiState: MissionControlUiState,
    onQuickUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MissionControlTokens.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { RelayStatusPill(isOnline = uiState.isRelayOnline) }
        item { DashboardHeader(uiState) }
        item { TelemetryGrid(uiState) }
        item { QuickUploadButton(onClick = onQuickUploadClick) }
        item {
            Text(
                text = "Active Queue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MissionControlTokens.TextPrimary
            )
        }
        if (uiState.activeTransfers.isEmpty()) {
            item { EmptyQueueCard() }
        } else {
            items(uiState.activeTransfers, key = { it.id }) { transfer ->
                ActiveTransferCard(transfer = transfer)
            }
        }
    }
}
@Composable
private fun RelayStatusPill(isOnline: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MissionControlTokens.CardSurface)
            .border(1.dp, MissionControlTokens.BorderOutline, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isOnline) MissionControlTokens.StatusSuccess
                    else MissionControlTokens.StatusError
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isOnline) "LOCAL RELAY ONLINE" else "RELAY OFFLINE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MissionControlTokens.TextPrimary
        )
    }
}

@Composable
private fun DashboardHeader(uiState: MissionControlUiState) {
    Text(
        text = "Mission Control",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MissionControlTokens.TextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = if (uiState.activeTransfersCount > 0) {
            "${uiState.activeTransfersCount} active transfers, protected by auto-resume."
        } else {
            "Ready - no active transfers in queue."
        },
        color = MissionControlTokens.TextSecondary,
        style = MaterialTheme.typography.bodyMedium
    )
}
@Composable
private fun TelemetryGrid(uiState: MissionControlUiState) {
    // reliabilityPercentage is null until a mapper measures real history, so a
    // fresh install must never render a reliability figure it cannot support.
    val reliabilityValue = uiState.reliabilityPercentage
        ?.takeIf { uiState.totalUploadedBytes > 0 }
        ?.let { String.format(Locale.US, "%.1f%%", it) }
        ?: "--"
    val reliabilitySubtext = when {
        uiState.reliabilityPercentage == null -> "No history yet"
        uiState.reliabilityPercentage >= 95f -> "Verified | Armed"
        else -> "Degraded"
    }
    val speedValue = if (uiState.currentSpeedBytesPerSec > 0) {
        "${formatBytes(uiState.currentSpeedBytesPerSec)}/s"
    } else {
        "--"
    }
    val weekSubtext = if (uiState.weeklyUploadedBytes > 0) {
        "+${formatBytes(uiState.weeklyUploadedBytes)} this week"
    } else {
        "No uploads this week"
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard("Reliability", reliabilityValue, reliabilitySubtext, Modifier.weight(1f))
            TelemetryCard(
                label = "Total Uploaded",
                value = formatBytes(uiState.totalUploadedBytes),
                subtext = weekSubtext,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard(
                label = "Active Transfers",
                value = String.format(Locale.US, "%02d", uiState.activeTransfersCount),
                subtext = if (uiState.activeTransfersCount > 0) "Auto-resume active" else "Standby",
                modifier = Modifier.weight(1f)
            )
            TelemetryCard("Speed", speedValue, "TDLib Stream", Modifier.weight(1f))
        }
    }
}
@Composable
private fun TelemetryCard(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, MissionControlTokens.BorderOutline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MissionControlTokens.CardSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, fontSize = 12.sp, color = MissionControlTokens.TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MissionControlTokens.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 11.sp, color = MissionControlTokens.AccentCyan)
        }
    }
}

@Composable
private fun QuickUploadButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MissionControlTokens.PrimaryLime,
            contentColor = MissionControlTokens.Background
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MissionControlTokens.Background
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Quick upload payload",
            color = MissionControlTokens.Background,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun EmptyQueueCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MissionControlTokens.BorderOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MissionControlTokens.CardSurface)
    ) {
        Box(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No files currently uploading",
                color = MissionControlTokens.TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
@Composable
private fun ActiveTransferCard(transfer: ActiveTransferItem) {
    val speedText = if (transfer.currentSpeedBytesPerSec > 0) {
        "${formatBytes(transfer.currentSpeedBytesPerSec)}/s"
    } else {
        "--"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MissionControlTokens.BorderOutline, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MissionControlTokens.CardSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transfer.fileName,
                    fontWeight = FontWeight.Bold,
                    color = MissionControlTokens.TextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "${(transfer.progress * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = MissionControlTokens.PrimaryLime,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${transfer.destinationName} - ${formatBytes(transfer.totalBytes)}",
                fontSize = 12.sp,
                color = MissionControlTokens.TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { transfer.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MissionControlTokens.PrimaryLime,
                trackColor = MissionControlTokens.BorderOutline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = speedText, fontSize = 12.sp, color = MissionControlTokens.AccentCyan)
                Text(
                    text = "${formatBytes(transfer.uploadedBytes)} / ${formatBytes(transfer.totalBytes)}",
                    fontSize = 12.sp,
                    color = MissionControlTokens.TextSecondary
                )
            }
        }
    }
}

/** Formats a byte count with a fixed locale so the decimal separator never varies. */
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val k = 1024.0
    val sizes = arrayOf("B", "KB", "MB", "GB", "TB")
    val i = (Math.log(bytes.toDouble()) / Math.log(k)).toInt().coerceIn(0, sizes.size - 1)
    return String.format(Locale.US, "%.1f %s", bytes / Math.pow(k, i.toDouble()), sizes[i])
}
