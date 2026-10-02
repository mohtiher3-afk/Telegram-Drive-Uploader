package com.telegramdrive.uploader.feature.missioncontrol.model

data class ActiveTransferItem(
    val id: String,
    val fileName: String,
    val destinationName: String,
    val uploadedBytes: Long,
    val totalBytes: Long,
    val currentSpeedBytesPerSec: Long
) {
    val progress: Float
        get() = if (totalBytes > 0) uploadedBytes.toFloat() / totalBytes else 0f
}

data class MissionControlUiState(
    val isRelayOnline: Boolean = false,
    val activeTransfersCount: Int = 0,
    val totalUploadedBytes: Long = 0L,
    val weeklyUploadedBytes: Long = 0L,
    val reliabilityPercentage: Float = 100f,
    val currentSpeedBytesPerSec: Long = 0L,
    val activeTransfers: List<ActiveTransferItem> = emptyList()
)