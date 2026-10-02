package com.telegramdrive.uploader.feature.missioncontrol.mapper

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.feature.missioncontrol.model.ActiveTransferItem
import com.telegramdrive.uploader.feature.missioncontrol.model.MissionControlUiState

/**
 * Builds the Mission Control state from real upload state.
 *
 * Two decisions worth stating, because both change what the dashboard claims:
 *
 * - reliabilityPercentage is null when nothing has finished yet. Returning 100f
 *   for "no data" would render a success badge that never happened.
 * - Speed is the sum of [UploadTask.averageSpeed] over active transfers, so a
 *   device with an idle queue shows 0 rather than a stale non-zero figure.
 */
fun mapToMissionControlState(
    tasks: List<UploadTask>,
    isRelayOnline: Boolean,
    destinationTitles: Map<Long, String> = emptyMap(),
    weekStartMillis: Long = System.currentTimeMillis()
): MissionControlUiState {
    val activeTasks = tasks.filter {
        it.status == UploadStatus.UPLOADING || it.status == UploadStatus.PAUSED
    }

    val totalUploadedBytes = tasks.sumOf { it.uploadedBytes }
    val currentSpeed = activeTasks.sumOf { it.averageSpeed }

    val completedCount = tasks.count { it.status == UploadStatus.COMPLETED }
    val failedCount = tasks.count { it.status == UploadStatus.FAILED }
    val totalFinished = completedCount + failedCount

    val reliabilityPercentage = if (totalFinished > 0) {
        completedCount.toFloat() / totalFinished.toFloat() * 100f
    } else {
        null
    }

    return MissionControlUiState(
        isRelayOnline = isRelayOnline,
        activeTransfersCount = activeTasks.size,
        totalUploadedBytes = totalUploadedBytes,
        weeklyUploadedBytes = tasks
            .filter { (it.completedAt ?: it.createdAt) >= weekStartMillis }
            .sumOf { it.uploadedBytes },
        reliabilityPercentage = reliabilityPercentage,
        currentSpeedBytesPerSec = currentSpeed,
        activeTransfers = activeTasks.map { task ->
            ActiveTransferItem(
                id = task.id,
                fileName = task.fileName,
                // UploadTask persists only destinationId; the display title lives in
                // TelegramDestination, so it is supplied by the caller.
                destinationName = destinationTitles[task.destinationId]
                    ?: "Chat ${task.destinationId}",
                uploadedBytes = task.uploadedBytes,
                totalBytes = task.totalBytes,
                currentSpeedBytesPerSec = task.averageSpeed
            )
        }
    )
}
