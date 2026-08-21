package com.telegramdrive.uploader.data.telegram.client

import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TelegramClient {
    val connectionState: StateFlow<TelegramConnectionState>
    val currentUser: StateFlow<TelegramUser?>
    val error: StateFlow<TelegramError?>
    val qrLoginLink: StateFlow<String?>
    
    val isConfigured: Boolean

    suspend fun connect()
    suspend fun sendPhoneNumber(phoneNumber: String)
    suspend fun sendCode(code: String)
    suspend fun sendPassword(password: String)
    suspend fun requestQrCodeLogin()
    suspend fun logout()
    fun clearError()
    
    fun getDestinations(query: String = ""): Flow<List<TelegramDestination>>
}
