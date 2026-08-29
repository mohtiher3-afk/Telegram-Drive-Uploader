package com.telegramdrive.uploader.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalVideosCount: Int = 0,
    val totalSize: Long = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val activeUploads: List<UploadTask> = emptyList(),
    val recentActivity: List<UploadTask> = emptyList(),
    val telegramConnectionState: TelegramConnectionState = TelegramConnectionState.DISCONNECTED,
    val telegramUser: TelegramUser? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val telegramRepository: TelegramRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        uploadRepository.getAllUploads(),
        telegramRepository.connectionState,
        telegramRepository.currentUser
    ) { uploads, connState, tgUser ->
        val totalCount = uploads.size
        val totalSize = uploads.sumOf { it.fileSize }
        val completed = uploads.count { it.status == UploadStatus.COMPLETED }
        val pending = uploads.count { it.status != UploadStatus.COMPLETED && it.status != UploadStatus.CANCELLED }
        
        val active = uploads.filter { 
            it.status == UploadStatus.QUEUED || 
            it.status == UploadStatus.PREPARING || 
            it.status == UploadStatus.UPLOADING || 
            it.status == UploadStatus.RETRYING 
        }
        
        val recent = uploads.filter { it.status == UploadStatus.COMPLETED || it.status == UploadStatus.FAILED || it.status == UploadStatus.CANCELLED }
            .take(5)

        HomeUiState(
            totalVideosCount = totalCount,
            totalSize = totalSize,
            completedCount = completed,
            pendingCount = pending,
            activeUploads = active,
            recentActivity = recent,
            telegramConnectionState = connState,
            telegramUser = tgUser
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

}

