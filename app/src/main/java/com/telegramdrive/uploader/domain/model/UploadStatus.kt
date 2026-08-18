package com.telegramdrive.uploader.domain.model

enum class UploadStatus {
    QUEUED,
    PREPARING,
    UPLOADING,
    PAUSED,
    RETRYING,
    COMPLETED,
    FAILED,
    CANCELLED
}
