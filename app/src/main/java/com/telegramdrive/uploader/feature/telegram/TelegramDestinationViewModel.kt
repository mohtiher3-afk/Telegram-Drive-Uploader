package com.telegramdrive.uploader.feature.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
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
    private val telegramRepository: TelegramRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val connectionState: StateFlow<TelegramConnectionState> = telegramRepository.connectionState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedDestination = MutableStateFlow<TelegramDestination?>(null)
    val selectedDestination: StateFlow<TelegramDestination?> = _selectedDestination

    val pinnedDestinationIds: StateFlow<Set<Long>> = settingsDataStore.pinnedDestinationIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val destinations: StateFlow<List<TelegramDestination>> = combine(
        _searchQuery
            .debounce(300)
            .flatMapLatest { query -> telegramRepository.getDestinations(query) },
        pinnedDestinationIds
    ) { results, pinnedIds ->
        results.sortedWith(
            compareByDescending<TelegramDestination> { it.id in pinnedIds }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectDestination(destination: TelegramDestination) {
        _selectedDestination.value = destination
        viewModelScope.launch {
            settingsDataStore.setSelectedDestination(destination.id, destination.title)
        }
    }

    fun setDestinationPinned(destinationId: Long, pinned: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setPinnedDestination(destinationId, pinned)
        }
    }

    fun clearSelection() {
        _selectedDestination.value = null
    }
}
