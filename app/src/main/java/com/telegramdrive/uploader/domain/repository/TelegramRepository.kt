package com.telegramdrive.uploader.domain.repository

import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TelegramRepository {
    val connectionState: StateFlow<TelegramConnectionState>
    val currentUser: StateFlow<TelegramUser?>
    val error: StateFlow<TelegramError?>
    val qrLoginLink: StateFlow<String?>
    val accounts: Flow<List<TelegramAccountEntry>>
    
    // Check if configuration uses placeholders or is fully configured
    val isConfigured: Boolean

    suspend fun connect()
    suspend fun sendPhoneNumber(phoneNumber: String)
    suspend fun sendCode(code: String)
    suspend fun sendPassword(password: String)
    suspend fun requestQrCodeLogin()
    suspend fun logout()
    suspend fun switchAccount(accountKey: String)
    fun clearError()
    
    fun getDestinations(query: String = ""): Flow<List<TelegramDestination>>
}
