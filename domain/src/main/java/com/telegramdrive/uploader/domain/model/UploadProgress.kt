package com.telegramdrive.uploader.domain.model

data class UploadProgress(
    val uploadedBytes: Long,
    val totalBytes: Long,
    val percentage: Float,
    val speedBytesPerSecond: Long,
    val averageSpeedBytesPerSecond: Long,
    val etaSeconds: Long
)
