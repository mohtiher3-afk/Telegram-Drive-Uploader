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
    /**
     * Best-effort cancellation of in-flight TDLib preliminary uploads tracked by
     * this client. Fire-and-forget: safe to call from worker cancellation paths.
     */
    fun cancelActiveUploads()
    /**
     * Removes and returns a buffered send-success for [oldMessageId], if a
     * confirmation arrived before anyone waited for it. Null otherwise.
     */
    fun takeBufferedSendSuccess(oldMessageId: Long): SendConfirmation?
    /**
     * Waits up to [timeoutMs] for `UpdateMessageSendSucceeded` matching
     * ([chatId], [oldMessageId]). Null on timeout — the message may still have been
     * sent, so callers must treat this as ambiguous and must NOT blind-resend.
     */
    suspend fun awaitSendConfirmation(chatId: Long, oldMessageId: Long, timeoutMs: Long): SendConfirmation?
}

sealed class TelegramUploadEvent {
    data class Progress(val uploadedBytes: Long, val totalBytes: Long) : TelegramUploadEvent()
    data class Completed(val messageLink: String?) : TelegramUploadEvent()
    data class Failed(val message: String, val retryable: Boolean) : TelegramUploadEvent()
    /**
     * TDLib accepted the SendMessage call and returned a provisional (local) message id.
     * This is NOT delivery proof — the engine must persist it and keep waiting for
     * [Completed]. Emitted before any terminal event of the same send.
     */
    data class MessageSent(val provisionalMessageId: Long) : TelegramUploadEvent()
}

/** Telegram-side confirmation of a previously sent provisional message. */
data class SendConfirmation(
    val chatId: Long,
    val messageId: Long,
    val messageLink: String?
)
