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
    // null means "no upload history yet". A 100f default would render as a
    // perfect-reliability badge on a fresh install, which is a success signal the
    // app has not earned.
    val reliabilityPercentage: Float? = null,
    val currentSpeedBytesPerSec: Long = 0L,
    val activeTransfers: List<ActiveTransferItem> = emptyList()
)