package com.telegramdrive.uploader.feature.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TelegramAuthViewModel @Inject constructor(
    private val telegramRepository: TelegramRepository
) : ViewModel() {

    val connectionState: StateFlow<TelegramConnectionState> = telegramRepository.connectionState
    val currentUser: StateFlow<TelegramUser?> = telegramRepository.currentUser
    val error: StateFlow<TelegramError?> = telegramRepository.error

    val isConfigured: Boolean = telegramRepository.isConfigured

    // Local UI input states that survive rotation
    val phoneNumberInput = MutableStateFlow("")
    val codeInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    fun connect() {
        viewModelScope.launch {
            _isProcessing.value = true
            telegramRepository.connect()
            _isProcessing.value = false
        }
    }

    fun sendPhoneNumber() {
        val phone = phoneNumberInput.value
        viewModelScope.launch {
            _isProcessing.value = true
            telegramRepository.sendPhoneNumber(phone)
            _isProcessing.value = false
        }
    }

    fun sendCode() {
        val code = codeInput.value
        viewModelScope.launch {
            _isProcessing.value = true
            telegramRepository.sendCode(code)
            _isProcessing.value = false
        }
    }

    fun sendPassword() {
        val password = passwordInput.value
        viewModelScope.launch {
            _isProcessing.value = true
            telegramRepository.sendPassword(password)
            _isProcessing.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isProcessing.value = true
            telegramRepository.logout()
            // Reset input fields on logout
            phoneNumberInput.value = ""
            codeInput.value = ""
            passwordInput.value = ""
            _isProcessing.value = false
        }
    }

    fun clearError() {
        telegramRepository.clearError()
    }
}
