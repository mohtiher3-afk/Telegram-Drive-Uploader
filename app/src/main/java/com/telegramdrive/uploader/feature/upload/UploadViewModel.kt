package com.telegramdrive.uploader.feature.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.core.util.VideoMetadataExtractor
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.upload.UploadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UploadUiState {
    object Idle : UploadUiState
    object Loading : UploadUiState
    data class Success(
        val preparedVideos: List<UploadTask>,
        val selectedDestination: TelegramDestination? = null
    ) : UploadUiState
    data class Error(val message: String) : UploadUiState
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val telegramRepository: TelegramRepository,
    private val uploadManager: UploadManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    val selectedDestination: StateFlow<TelegramDestination?> = _uiState.map {
        if (it is UploadUiState.Success) it.selectedDestination else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _preparedList = mutableListOf<UploadTask>()
    private var _selectedDestination: TelegramDestination? = null

    fun onDestinationSelected(destination: TelegramDestination) {
        selectDestination(destination)
    }

    fun setPrepareUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _uiState.value = UploadUiState.Loading
        _preparedList.clear()

        viewModelScope.launch {
            try {
                uris.forEach { uri ->
                    val task = VideoMetadataExtractor.extractMetadata(context, uri)
                    
                    // Avoid duplicate selection within current prep flow
                    if (_preparedList.none { it.sourceUri == task.sourceUri }) {
                        _preparedList.add(task)
                        DiagnosticsManager.log(
                            category = DiagnosticCategory.UPLOAD_CREATED,
                            severity = DiagnosticSeverity.INFO,
                            message = "Prepared video metadata (filename masked for privacy). Bytes size: ${task.totalBytes}",
                            uploadId = task.id
                        )
                    }
                }
                _uiState.value = UploadUiState.Success(_preparedList.toList(), _selectedDestination)
            } catch (e: Exception) {
                _uiState.value = UploadUiState.Error(e.message ?: "Failed to extract metadata")
                DiagnosticsManager.log(
                    category = DiagnosticCategory.UPLOAD_FAILED,
                    severity = DiagnosticSeverity.ERROR,
                    message = "Failed to extract metadata from selected video URI.",
                    errorCode = ErrorCode.SOURCE_FILE_UNAVAILABLE,
                    exception = e
                )
            }
        }
    }

    fun selectDestination(destination: TelegramDestination) {
        _selectedDestination = destination
        val current = _uiState.value
        if (current is UploadUiState.Success) {
            _uiState.value = current.copy(selectedDestination = destination)
        }
    }

    fun removePreparedVideo(video: UploadTask) {
        _preparedList.removeAll { it.id == video.id }
        _uiState.value = UploadUiState.Success(_preparedList.toList(), _selectedDestination)
    }

    fun addToQueue(onComplete: () -> Unit) {
        val destination = _selectedDestination ?: return
        
        viewModelScope.launch {
            if (_preparedList.isNotEmpty()) {
                try {
                    val tasksToInsert = _preparedList.map { 
                        it.copy(destinationId = destination.id)
                    }
                    tasksToInsert.forEach { 
                        uploadRepository.insertUpload(it)
                        uploadManager.enqueueUpload(it)
                        DiagnosticsManager.log(
                            category = DiagnosticCategory.UPLOAD_CREATED,
                            severity = DiagnosticSeverity.INFO,
                            message = "Upload task inserted in Local DB and enqueued successfully.",
                            uploadId = it.id
                        )
                    }
                    
                    _preparedList.clear()
                    _selectedDestination = null
                    _uiState.value = UploadUiState.Idle
                    onComplete()
                } catch (e: Exception) {
                    DiagnosticsManager.log(
                        category = DiagnosticCategory.DATABASE_ERROR,
                        severity = DiagnosticSeverity.ERROR,
                        message = "Failed to insert upload tasks to local database.",
                        errorCode = ErrorCode.DATABASE_FAILURE,
                        exception = e
                    )
                }
            }
        }
    }
}
