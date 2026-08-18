package com.telegramdrive.uploader.domain.upload

import com.telegramdrive.uploader.domain.model.UploadTask
import kotlinx.coroutines.flow.Flow

interface UploadManager {
    fun enqueueUpload(task: UploadTask)
    fun pauseUpload(id: String)
    fun resumeUpload(task: UploadTask)
    fun cancelUpload(id: String)
    fun retryUpload(task: UploadTask)
    fun observeUpload(id: String): Flow<UploadTask?>
    fun observeUploads(): Flow<List<UploadTask>>
}
