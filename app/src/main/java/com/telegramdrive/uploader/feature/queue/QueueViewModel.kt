package com.telegramdrive.uploader.feature.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class QueueFilter {
    ALL,
    ACTIVE,
    PAUSED,
    FAILED
}

data class QueueUiState(
    val queueItems: List<UploadTask> = emptyList(),
    val selectedFilter: QueueFilter = QueueFilter.ALL,
    val query: String = "",
    val totalMatches: Int = 0,
    val failedCount: Int = 0,
    val pausedCount: Int = 0,
    val activeCount: Int = 0
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val uploadManager: UploadManager
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(QueueFilter.ALL)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<QueueUiState> = combine(
        uploadRepository.getAllUploads(),
        selectedFilter,
        query
    ) { uploads, filter, queryText ->
        val pending = uploads.filter {
            it.status != UploadStatus.COMPLETED && it.status != UploadStatus.CANCELLED
        }
        val filtered = when (filter) {
            QueueFilter.ALL -> pending
            QueueFilter.ACTIVE -> pending.filter {
                it.status == UploadStatus.QUEUED ||
                    it.status == UploadStatus.PREPARING ||
                    it.status == UploadStatus.UPLOADING ||
                    it.status == UploadStatus.RETRYING
            }
            QueueFilter.PAUSED -> pending.filter { it.status == UploadStatus.PAUSED }
            QueueFilter.FAILED -> pending.filter { it.status == UploadStatus.FAILED }
        }
        val normalized = queryText.trim().lowercase()
        val matched = if (normalized.isBlank()) {
            filtered
        } else {
            filtered.filter {
                it.fileName.lowercase().contains(normalized) || it.status.name.lowercase().contains(normalized)
            }
        }
        QueueUiState(
            queueItems = matched,
            selectedFilter = filter,
            query = queryText,
            totalMatches = matched.size,
            failedCount = pending.count { it.status == UploadStatus.FAILED },
            pausedCount = pending.count { it.status == UploadStatus.PAUSED },
            activeCount = pending.count {
                it.status == UploadStatus.QUEUED ||
                    it.status == UploadStatus.PREPARING ||
                    it.status == UploadStatus.UPLOADING ||
                    it.status == UploadStatus.RETRYING
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QueueUiState()
    )

    fun selectFilter(filter: QueueFilter) {
        selectedFilter.value = filter
    }

    fun onQueryChanged(value: String) {
        query.value = value
    }

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

    fun retryAllFailed() {
        viewModelScope.launch {
            uploadRepository.getAllUploads().first()
                .filter { it.status == UploadStatus.FAILED }
                .forEach { task ->
                    uploadRepository.updateStatus(task.id, UploadStatus.RETRYING)
                    uploadManager.retryUpload(task)
                }
        }
    }

    fun pauseAllActive() {
        viewModelScope.launch {
            uploadRepository.getAllUploads().first()
                .filter {
                    it.status == UploadStatus.QUEUED ||
                        it.status == UploadStatus.PREPARING ||
                        it.status == UploadStatus.UPLOADING ||
                        it.status == UploadStatus.RETRYING
                }
                .forEach { task -> pauseUpload(task.id) }
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
