package com.telegramdrive.uploader.data.telegram.client

import com.telegramdrive.uploader.data.local.datastore.TelegramAccountEntry
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.model.UploadTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TelegramClient {
    val connectionState: StateFlow<TelegramConnectionState>
    val currentUser: StateFlow<TelegramUser?>
    val error: StateFlow<TelegramError?>
    val qrLoginLink: StateFlow<String?>
    val accounts: Flow<List<TelegramAccountEntry>>

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
    fun uploadLocalDocument(task: UploadTask, localPath: String): Flow<TelegramUploadEvent>
}

sealed class TelegramUploadEvent {
    data class Progress(val uploadedBytes: Long, val totalBytes: Long) : TelegramUploadEvent()
    data class Completed(val messageLink: String?) : TelegramUploadEvent()
    data class Failed(val message: String, val retryable: Boolean) : TelegramUploadEvent()
}
