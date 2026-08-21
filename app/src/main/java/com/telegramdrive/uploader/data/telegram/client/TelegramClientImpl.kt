package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
import android.os.Build
import com.telegramdrive.uploader.BuildConfig
import com.telegramdrive.uploader.core.datastore.SettingsDataStore
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import com.telegramdrive.uploader.domain.model.TelegramConnectionState
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.domain.model.TelegramError
import com.telegramdrive.uploader.domain.model.TelegramUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramClientImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) : TelegramClient {
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val clientLock = Any()
    private val _connectionState = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
    private val _currentUser = MutableStateFlow<TelegramUser?>(null)
    private val _error = MutableStateFlow<TelegramError?>(null)
    private val _qrLoginLink = MutableStateFlow<String?>(null)
    private val chatLock = Any()
    private val chats = LinkedHashMap<Long, TdApi.Chat>()
    private val _chatDestinations = MutableStateFlow<List<TelegramDestination>>(emptyList())
    private val chatsRequested = AtomicBoolean(false)
    private var tdClient: Client? = null

    override val connectionState: StateFlow<TelegramConnectionState> = _connectionState.asStateFlow()
    override val currentUser: StateFlow<TelegramUser?> = _currentUser.asStateFlow()
    override val error: StateFlow<TelegramError?> = _error.asStateFlow()
    override val qrLoginLink: StateFlow<String?> = _qrLoginLink.asStateFlow()

    override val isConfigured: Boolean
        get() = BuildConfig.TELEGRAM_API_ID.toIntOrNull()?.let { it > 0 } == true &&
            BuildConfig.TELEGRAM_API_HASH.isNotBlank() &&
            BuildConfig.TELEGRAM_API_HASH != "placeholder_hash"

    override suspend fun connect() {
        if (!isConfigured) {
            fail(TelegramError.InvalidCredentials, "Telegram API credentials are not configured.")
            return
        }
        if (_connectionState.value == TelegramConnectionState.CONNECTING ||
            _connectionState.value == TelegramConnectionState.AUTHORIZED
        ) return

        _connectionState.value = TelegramConnectionState.CONNECTING
        _error.value = null
        _qrLoginLink.value = null
        try {
            ensureNativeRuntime()
            synchronized(clientLock) {
                if (tdClient == null) {
                    tdClient = Client.create(
                        { update -> handleTdLibObject(update) },
                        { throwable -> reportCallbackFailure(throwable) },
                        { throwable -> reportCallbackFailure(throwable) }
                    )
                }
            }
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.INFO,
                message = "Official TDLib v1.8.66 client created; waiting for authorization state."
            )
        } catch (failure: Throwable) {
            synchronized(clientLock) { tdClient = null }
            fail(
                TelegramError.TdLibRuntimeUnavailable,
                "TDLib native runtime could not be initialized: ${failure.javaClass.simpleName}"
            )
        }
    }

    override suspend fun sendPhoneNumber(phoneNumber: String) {
        val normalized = phoneNumber.trim()
        if (normalized.isBlank() || !normalized.startsWith("+")) {
            fail(TelegramError.InvalidPhoneNumber, "Invalid phone number format.")
            return
        }
        send(TdApi.SetAuthenticationPhoneNumber(
            normalized,
            TdApi.PhoneNumberAuthenticationSettings(false, false, false, false, false, null, null)
        ))
    }

    override suspend fun sendCode(code: String) {
        val normalized = code.trim()
        if (normalized.isBlank()) {
            fail(TelegramError.InvalidCode, "Verification code is empty.")
            return
        }
        send(TdApi.CheckAuthenticationCode(normalized))
    }

    override suspend fun sendPassword(password: String) {
        if (password.isBlank()) {
            fail(TelegramError.InvalidPassword, "Two-step verification password is empty.")
            return
        }
        send(TdApi.CheckAuthenticationPassword(password))
    }

    override suspend fun requestQrCodeLogin() {
        if (tdClient == null) {
            fail(TelegramError.TdLibRuntimeUnavailable, "TDLib client is not initialized.")
            return
        }
        _error.value = null
        send(TdApi.RequestQrCodeAuthentication(longArrayOf()))
    }

    override suspend fun logout() {
        _connectionState.value = TelegramConnectionState.CLOSING
        try {
            tdClient?.send(TdApi.LogOut(), { result -> handleTdLibObject(result) }, null)
        } catch (failure: Throwable) {
            reportCallbackFailure(failure)
        } finally {
            synchronized(clientLock) { tdClient = null }
            _currentUser.value = null
            _error.value = null
            _qrLoginLink.value = null
            synchronized(chatLock) { chats.clear() }
            _chatDestinations.value = emptyList()
            chatsRequested.set(false)
            _connectionState.value = TelegramConnectionState.DISCONNECTED
            settingsDataStore.clearTelegramUser()
            settingsDataStore.setTelegramConnectionState(TelegramConnectionState.DISCONNECTED.name)
        }
    }

    override fun clearError() {
        _error.value = null
    }

    override fun getDestinations(query: String): Flow<List<TelegramDestination>> =
        _chatDestinations.map { destinations ->
            val normalized = query.trim().lowercase(Locale.US)
            if (normalized.isBlank()) destinations
            else destinations.filter { destination ->
                destination.title.lowercase(Locale.US).contains(normalized) ||
                    destination.username?.lowercase(Locale.US)?.contains(normalized) == true
            }
        }.distinctUntilChanged()

    private suspend fun send(function: TdApi.Function<*>) {
        val client = tdClient
        if (client == null) {
            fail(TelegramError.TdLibRuntimeUnavailable, "TDLib client is not initialized.")
            return
        }
        try {
            withContext(Dispatchers.Default) {
                client.send(function, { result -> handleTdLibObject(result) }, null)
            }
        } catch (failure: Throwable) {
            reportCallbackFailure(failure)
        }
    }

    private fun handleTdLibObject(objectValue: TdApi.Object) {
        when (objectValue) {
            is TdApi.UpdateAuthorizationState -> handleAuthorizationState(objectValue.authorizationState)
            is TdApi.UpdateNewChat -> upsertChat(objectValue.chat)
            is TdApi.UpdateChatTitle -> updateChatTitle(objectValue)
            is TdApi.UpdateChatPermissions -> updateChatPermissions(objectValue)
            is TdApi.Chats -> objectValue.chatIds.forEach(::requestChat)
            is TdApi.Chat -> upsertChat(objectValue)
            is TdApi.User -> handleAuthenticatedUser(objectValue)
            is TdApi.Error -> mapError(objectValue)
        }
    }

    private fun handleAuthorizationState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> sendTdlibParameters()
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _qrLoginLink.value = null
                setState(TelegramConnectionState.WAITING_FOR_PHONE)
            }
            is TdApi.AuthorizationStateWaitCode -> setState(TelegramConnectionState.WAITING_FOR_CODE)
            is TdApi.AuthorizationStateWaitPassword -> setState(TelegramConnectionState.WAITING_FOR_PASSWORD)
            is TdApi.AuthorizationStateWaitOtherDeviceConfirmation -> {
                _qrLoginLink.value = state.link
                setState(TelegramConnectionState.WAITING_FOR_QR)
            }
            is TdApi.AuthorizationStateReady -> {
                setState(TelegramConnectionState.AUTHORIZED)
                tdClient?.send(TdApi.GetMe(), { result -> handleTdLibObject(result) }, null)
                requestChats()
            }
            is TdApi.AuthorizationStateClosing -> setState(TelegramConnectionState.CLOSING)
            is TdApi.AuthorizationStateClosed -> setState(TelegramConnectionState.DISCONNECTED)
            else -> fail(
                TelegramError.Unknown("Unsupported Telegram authorization state: ${state.javaClass.simpleName}"),
                "Unsupported TDLib authorization state."
            )
        }
    }

    private fun sendTdlibParameters() {
        val apiId = BuildConfig.TELEGRAM_API_ID.toIntOrNull()
        val apiHash = BuildConfig.TELEGRAM_API_HASH
        if (apiId == null || apiId <= 0 || apiHash.isBlank()) {
            fail(TelegramError.InvalidCredentials, "Telegram API credentials are invalid.")
            return
        }
        val databaseDirectory = File(context.filesDir, "tdlib-database").apply { mkdirs() }
        val filesDirectory = File(context.filesDir, "tdlib-files").apply { mkdirs() }
        val parameters = TdApi.SetTdlibParameters(
            false,
            databaseDirectory.absolutePath,
            filesDirectory.absolutePath,
            ByteArray(0),
            true,
            true,
            true,
            false,
            apiId,
            apiHash,
            Locale.getDefault().toLanguageTag(),
            Build.MODEL ?: "Android",
            Build.VERSION.RELEASE ?: "Android",
            BuildConfig.VERSION_NAME
        )
        tdClient?.send(parameters, { result -> handleTdLibObject(result) }, null)
    }

    private fun requestChats() {
        val client = tdClient ?: return
        if (_connectionState.value != TelegramConnectionState.AUTHORIZED) return
        if (!chatsRequested.compareAndSet(false, true)) return
        client.send(
            TdApi.GetChats(TdApi.ChatListMain(), 100),
            { result -> handleTdLibObject(result) },
            null
        )
    }

    private fun requestChat(chatId: Long) {
        tdClient?.send(TdApi.GetChat(chatId), { result -> handleTdLibObject(result) }, null)
    }

    private fun upsertChat(chat: TdApi.Chat) {
        synchronized(chatLock) { chats[chat.id] = chat }
        rebuildDestinations()
    }

    private fun updateChatTitle(update: TdApi.UpdateChatTitle) {
        synchronized(chatLock) { chats[update.chatId]?.title = update.title }
        rebuildDestinations()
    }

    private fun updateChatPermissions(update: TdApi.UpdateChatPermissions) {
        synchronized(chatLock) { chats[update.chatId]?.permissions = update.permissions }
        rebuildDestinations()
    }

    private fun rebuildDestinations() {
        val destinations = synchronized(chatLock) {
            chats.values.mapNotNull { chat ->
                val type = when (val chatType = chat.type) {
                    is TdApi.ChatTypePrivate -> TelegramDestinationType.USER
                    is TdApi.ChatTypeBasicGroup -> TelegramDestinationType.GROUP
                    is TdApi.ChatTypeSupergroup -> if (chatType.isChannel) {
                        TelegramDestinationType.CHANNEL
                    } else {
                        TelegramDestinationType.SUPERGROUP
                    }
                    else -> null
                } ?: return@mapNotNull null
                val canSend = chat.type is TdApi.ChatTypePrivate ||
                    chat.permissions?.canSendBasicMessages == true
                if (!canSend) return@mapNotNull null
                TelegramDestination(
                    id = chat.id,
                    title = chat.title.ifBlank { "Telegram chat" },
                    username = null,
                    type = type,
                    photo = null,
                    canSendMessages = true
                )
            }.sortedBy { it.title.lowercase(Locale.US) }
        }
        _chatDestinations.value = destinations
    }

    private fun handleAuthenticatedUser(user: TdApi.User) {
        val model = TelegramUser(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            username = user.usernames?.editableUsername
                ?: user.usernames?.activeUsernames?.firstOrNull(),
            phoneNumber = user.phoneNumber,
            profilePhoto = null
        )
        _currentUser.value = model
        clientScope.launch {
            settingsDataStore.saveTelegramUser(
                user.id,
                user.firstName,
                user.lastName,
                user.usernames?.editableUsername
                    ?: user.usernames?.activeUsernames?.firstOrNull(),
                user.phoneNumber
            )
            settingsDataStore.setTelegramConnectionState(TelegramConnectionState.AUTHORIZED.name)
        }
    }

    private fun mapError(error: TdApi.Error) {
        val message = error.message.uppercase(Locale.US)
        val mapped = when {
            message.contains("UPDATE_APP_TO_LOGIN") || error.code == 406 -> TelegramError.AppUpdateRequired
            message.contains("PHONE_NUMBER") -> TelegramError.InvalidPhoneNumber
            message.contains("PHONE_CODE") || message.contains("CODE") -> TelegramError.InvalidCode
            message.contains("PASSWORD") -> TelegramError.InvalidPassword
            error.code == 420 -> TelegramError.RateLimited
            error.code == 401 -> TelegramError.SessionExpired
            error.code in 500..599 -> TelegramError.NetworkUnavailable
            else -> TelegramError.Unknown(error.message)
        }
        fail(mapped, "TDLib error ${error.code}: ${error.message}")
    }

    private fun ensureNativeRuntime() {
        if (nativeLoaded.compareAndSet(false, true)) {
            try {
                System.loadLibrary("tdjni")
            } catch (failure: Throwable) {
                nativeLoaded.set(false)
                throw failure
            }
        }
    }

    private fun setState(state: TelegramConnectionState) {
        _connectionState.value = state
        clientScope.launch { settingsDataStore.setTelegramConnectionState(state.name) }
    }

    private fun fail(error: TelegramError, diagnosticMessage: String) {
        _error.value = error
        _connectionState.value = TelegramConnectionState.ERROR
        DiagnosticsManager.log(
            category = DiagnosticCategory.TELEGRAM_AUTH_ERROR,
            severity = DiagnosticSeverity.ERROR,
            message = diagnosticMessage,
            errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
        )
    }

    private fun reportCallbackFailure(failure: Throwable) {
        DiagnosticsManager.log(
            category = DiagnosticCategory.TELEGRAM_INIT,
            severity = DiagnosticSeverity.ERROR,
            message = "TDLib callback failed: ${failure.javaClass.simpleName}",
            errorCode = ErrorCode.TELEGRAM_UNAVAILABLE,
            exception = failure
        )
    }

    companion object {
        private val nativeLoaded = AtomicBoolean(false)
    }
}
