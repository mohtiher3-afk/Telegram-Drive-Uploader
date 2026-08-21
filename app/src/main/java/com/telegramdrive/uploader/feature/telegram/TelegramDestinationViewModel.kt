package com.telegramdrive.uploader.feature.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class TelegramDestinationViewModel @Inject constructor(
    private val telegramRepository: TelegramRepository
) : ViewModel() {

    val connectionState: StateFlow<TelegramConnectionState> = telegramRepository.connectionState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDestination = MutableStateFlow<TelegramDestination?>(null)
    val selectedDestination: StateFlow<TelegramDestination?> = _selectedDestination

    val destinations: StateFlow<List<TelegramDestination>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            telegramRepository.getDestinations(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectDestination(destination: TelegramDestination) {
        _selectedDestination.value = destination
    }

    fun clearSelection() {
        _selectedDestination.value = null
    }
}
