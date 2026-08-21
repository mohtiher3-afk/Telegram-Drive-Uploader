package com.telegramdrive.uploader.data.telegram.repository

import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramRepositoryImpl @Inject constructor(
    private val telegramClient: TelegramClient
) : TelegramRepository {

    override val connectionState: StateFlow<TelegramConnectionState> = telegramClient.connectionState
    override val currentUser: StateFlow<TelegramUser?> = telegramClient.currentUser
    override val error: StateFlow<TelegramError?> = telegramClient.error
    override val qrLoginLink: StateFlow<String?> = telegramClient.qrLoginLink
    
    override val isConfigured: Boolean
        get() = telegramClient.isConfigured

    override suspend fun connect() = telegramClient.connect()
    override suspend fun sendPhoneNumber(phoneNumber: String) = telegramClient.sendPhoneNumber(phoneNumber)
    override suspend fun sendCode(code: String) = telegramClient.sendCode(code)
    override suspend fun sendPassword(password: String) = telegramClient.sendPassword(password)
    override suspend fun requestQrCodeLogin() = telegramClient.requestQrCodeLogin()
    override suspend fun logout() = telegramClient.logout()
    override fun clearError() = telegramClient.clearError()
    
    override fun getDestinations(query: String): Flow<List<TelegramDestination>> = telegramClient.getDestinations(query)
}
