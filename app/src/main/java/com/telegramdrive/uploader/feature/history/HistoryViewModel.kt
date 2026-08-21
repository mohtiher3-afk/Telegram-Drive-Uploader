package com.telegramdrive.uploader.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class HistoryPeriod {
    ALL,
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS
}

enum class HistorySort {
    NEWEST,
    LARGEST
}

data class HistoryUiState(
    val historyItems: List<UploadTask> = emptyList(),
    val query: String = "",
    val period: HistoryPeriod = HistoryPeriod.ALL,
    val sort: HistorySort = HistorySort.NEWEST,
    val totalMatches: Int = 0,
    val totalSize: Long = 0L
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val uploadRepository: UploadRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val period = MutableStateFlow(HistoryPeriod.ALL)
    private val sort = MutableStateFlow(HistorySort.NEWEST)

    val uiState: StateFlow<HistoryUiState> = combine(
        uploadRepository.getAllUploads(), query, period, sort
    ) { uploads, text, selectedPeriod, selectedSort ->
        val now = System.currentTimeMillis()
        val start = when (selectedPeriod) {
            HistoryPeriod.ALL -> 0L
            HistoryPeriod.TODAY -> Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            HistoryPeriod.LAST_7_DAYS -> now - 7L * 24 * 60 * 60 * 1000
            HistoryPeriod.LAST_30_DAYS -> now - 30L * 24 * 60 * 60 * 1000
        }
        val normalized = text.trim().lowercase()
        val matches = uploads
            .asSequence()
            .filter { it.status == UploadStatus.COMPLETED }
            .filter { (it.completedAt ?: it.createdAt) >= start }
            .filter { normalized.isBlank() || it.fileName.lowercase().contains(normalized) }
            .let { sequence ->
                when (selectedSort) {
                    HistorySort.NEWEST -> sequence.sortedByDescending { it.completedAt ?: it.createdAt }
                    HistorySort.LARGEST -> sequence.sortedByDescending { it.fileSize }
                }
            }
            .toList()
        HistoryUiState(
            historyItems = matches,
            query = text,
            period = selectedPeriod,
            sort = selectedSort,
            totalMatches = matches.size,
            totalSize = matches.sumOf { it.fileSize }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun onQueryChanged(value: String) { query.value = value }
    fun setPeriod(value: HistoryPeriod) { period.value = value }
    fun setSort(value: HistorySort) { sort.value = value }

    fun deleteUpload(id: String) {
        viewModelScope.launch { uploadRepository.deleteUploadById(id) }
    }

    fun clearHistory() {
        viewModelScope.launch { uploadRepository.deleteCompletedUploads() }
    }
}
