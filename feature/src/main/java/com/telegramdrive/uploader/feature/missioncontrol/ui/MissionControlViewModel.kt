package com.telegramdrive.uploader.feature.missioncontrol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import com.telegramdrive.uploader.feature.missioncontrol.mapper.mapToMissionControlState
import com.telegramdrive.uploader.feature.missioncontrol.model.MissionControlUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Supplies Mission Control from real upload and Telegram state.
 *
 * "Relay online" is derived from [TelegramConnectionState] rather than from a
 * separate monitor, so the badge can never claim a live connection the client
 * does not actually hold.
 */
@HiltViewModel
class MissionControlViewModel @Inject constructor(
    uploadRepository: UploadRepository,
    telegramRepository: TelegramRepository
) : ViewModel() {

    val uiState: StateFlow<MissionControlUiState> = combine(
        uploadRepository.getAllUploads(),
        telegramRepository.connectionState
    ) { tasks, connectionState ->
        mapToMissionControlState(
            tasks = tasks,
            isRelayOnline = connectionState == TelegramConnectionState.AUTHORIZED
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MissionControlUiState()
    )
}
