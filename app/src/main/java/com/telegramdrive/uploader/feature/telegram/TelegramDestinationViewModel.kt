package com.telegramdrive.uploader.feature.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DestinationFilter {
    ALL,
    SAVED_MESSAGES,
    CHANNELS,
    GROUPS
}

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class TelegramDestinationViewModel @Inject constructor(
    private val telegramRepository: TelegramRepository
) : ViewModel() {

    val connectionState: StateFlow<TelegramConnectionState> = telegramRepository.connectionState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(DestinationFilter.ALL)
    val selectedFilter: StateFlow<DestinationFilter> = _selectedFilter

    private val _selectedDestination = MutableStateFlow<TelegramDestination?>(null)
    val selectedDestination: StateFlow<TelegramDestination?> = _selectedDestination

    private val _refreshTrigger = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val rawDestinations: StateFlow<List<TelegramDestination>> = combine(_searchQuery.debounce(250), _refreshTrigger) { query, _ -> query }
        .flatMapLatest { query ->
            _isLoading.value = true
            telegramRepository.getDestinations(query)
                .onEach { _isLoading.value = false }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val destinations: StateFlow<List<TelegramDestination>> = combine(rawDestinations, _selectedFilter) { list, filter ->
        when (filter) {
            DestinationFilter.ALL -> list
            DestinationFilter.SAVED_MESSAGES -> list.filter { it.type == TelegramDestinationType.USER && it.title == "Saved Messages" }
            DestinationFilter.CHANNELS -> list.filter { it.type == TelegramDestinationType.CHANNEL }
            DestinationFilter.GROUPS -> list.filter { it.type == TelegramDestinationType.GROUP || it.type == TelegramDestinationType.SUPERGROUP }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: DestinationFilter) {
        _selectedFilter.value = filter
    }

    fun selectDestination(destination: TelegramDestination) {
        _selectedDestination.value = destination
    }

    fun clearSelection() {
        _selectedDestination.value = null
    }

    fun refresh() {
        _refreshTrigger.value += 1
    }
}
