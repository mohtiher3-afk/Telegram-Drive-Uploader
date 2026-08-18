package com.telegramdrive.uploader.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val historyItems: List<UploadTask> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val uploadRepository: UploadRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = uploadRepository.getAllUploads()
        .map { uploads ->
            val completedItems = uploads.filter { it.status == UploadStatus.COMPLETED }
            HistoryUiState(historyItems = completedItems)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )

    fun deleteUpload(id: String) {
        viewModelScope.launch {
            uploadRepository.deleteUploadById(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            uploadRepository.deleteCompletedUploads()
        }
    }
}
