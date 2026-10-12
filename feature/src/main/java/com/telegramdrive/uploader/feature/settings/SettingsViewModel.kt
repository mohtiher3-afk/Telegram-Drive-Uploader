package com.telegramdrive.uploader.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.ui.components.formatFileSize
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val theme: String = "System",
    val dynamicColorEnabled: Boolean = true,
    val cacheSize: String = "0 B",
    val telegramConnectionState: TelegramConnectionState = TelegramConnectionState.DISCONNECTED,
    val telegramUser: TelegramUser? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val telegramRepository: TelegramRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _cacheSizeFlow = MutableStateFlow("0 B")

    init {
        updateCacheSize()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.themePreference,
        settingsDataStore.dynamicColorEnabled,
        _cacheSizeFlow,
        telegramRepository.connectionState,
        telegramRepository.currentUser
    ) { array ->
        SettingsUiState(
            theme = array[0] as String,
            dynamicColorEnabled = array[1] as Boolean,
            cacheSize = array[2] as String,
            telegramConnectionState = array[3] as TelegramConnectionState,
            telegramUser = array[4] as TelegramUser?
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsDataStore.setThemePreference(theme)
            DiagnosticsManager.log(
                category = DiagnosticCategory.SETTINGS_CHANGED,
                severity = DiagnosticSeverity.INFO,
                message = "Application theme changed to: $theme"
            )
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDynamicColorEnabled(enabled)
            DiagnosticsManager.log(
                category = DiagnosticCategory.SETTINGS_CHANGED,
                severity = DiagnosticSeverity.INFO,
                message = "Dynamic color changed to: $enabled"
            )
        }
    }

    fun updateCacheSize() {
        viewModelScope.launch {
            val sizeStr = withContext(Dispatchers.IO) {
                val thumbDir = File(context.cacheDir, "thumbnails")
                if (!thumbDir.exists()) "0 B"
                else {
                    val sizeBytes = thumbDir.walk().filter { it.isFile }.sumOf { it.length() }
                    formatFileSize(sizeBytes)
                }
            }
            _cacheSizeFlow.value = sizeStr
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val thumbDir = File(context.cacheDir, "thumbnails")
                if (thumbDir.exists()) {
                    thumbDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            }
            DiagnosticsManager.log(
                category = DiagnosticCategory.SETTINGS_CHANGED,
                severity = DiagnosticSeverity.INFO,
                message = "Local thumbnail cache cleared successfully."
            )
            updateCacheSize()
        }
    }

    fun logoutTelegram() {
        viewModelScope.launch {
            telegramRepository.logout()
        }
    }
}
