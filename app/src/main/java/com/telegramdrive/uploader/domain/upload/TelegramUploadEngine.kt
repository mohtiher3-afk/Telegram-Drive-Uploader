package com.telegramdrive.uploader.domain.upload

import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadTask
import kotlinx.coroutines.flow.Flow

interface TelegramUploadEngine {
    fun uploadFile(task: UploadTask): Flow<UploadEngineResult>
}

sealed class UploadEngineResult {
    data class Progress(val progress: UploadProgress) : UploadEngineResult()
    data class Success(val uploadDurationMs: Long, val messageLink: String? = null) : UploadEngineResult()
    data class Error(val message: String, val isRetryable: Boolean) : UploadEngineResult()
}
