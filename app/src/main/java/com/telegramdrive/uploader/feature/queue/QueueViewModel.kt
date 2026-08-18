package com.telegramdrive.uploader.feature.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueueUiState(
    val queueItems: List<UploadTask> = emptyList()
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val uploadManager: UploadManager
) : ViewModel() {

    val uiState: StateFlow<QueueUiState> = uploadRepository.getAllUploads()
        .map { uploads ->
            val pendingItems = uploads.filter { it.status != UploadStatus.COMPLETED && it.status != UploadStatus.CANCELLED }
            QueueUiState(queueItems = pendingItems)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QueueUiState()
        )

    fun pauseUpload(id: String) {
        viewModelScope.launch {
            uploadRepository.updateStatus(id, UploadStatus.PAUSED)
            uploadManager.pauseUpload(id)
        }
    }

    fun resumeUpload(id: String) {
        viewModelScope.launch {
            uploadRepository.getUploadById(id)?.let {
                uploadRepository.updateStatus(id, UploadStatus.QUEUED)
                uploadManager.resumeUpload(it)
            }
        }
    }

    fun retryUpload(id: String) {
        viewModelScope.launch {
            uploadRepository.getUploadById(id)?.let {
                uploadRepository.updateStatus(id, UploadStatus.RETRYING)
                uploadManager.retryUpload(it)
            }
        }
    }

    fun cancelUpload(id: String) {
        viewModelScope.launch {
            uploadRepository.updateStatus(id, UploadStatus.CANCELLED)
            uploadManager.cancelUpload(id)
        }
    }

    fun removeUpload(id: String) {
        viewModelScope.launch {
            uploadManager.cancelUpload(id)
            uploadRepository.deleteUploadById(id)
        }
    }
}
