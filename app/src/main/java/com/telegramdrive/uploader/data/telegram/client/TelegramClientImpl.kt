package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelegramClientImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : TelegramClient {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _connectionState = MutableStateFlow(TelegramConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<TelegramConnectionState> = _connectionState.asStateFlow()

    private val _currentUser = MutableStateFlow<TelegramUser?>(null)
    override val currentUser: StateFlow<TelegramUser?> = _currentUser.asStateFlow()

    private val _error = MutableStateFlow<TelegramError?>(null)
    override val error: StateFlow<TelegramError?> = _error.asStateFlow()

    override val isConfigured: Boolean
        get() = try {
            BuildConfig.TELEGRAM_API_ID.toIntOrNull() != null && BuildConfig.TELEGRAM_API_HASH.isNotBlank()
        } catch (_: Exception) {
            false
        }

    private var tdLibClient: TdLibClient? = null

    init {
        scope.launch {
            val savedUserStr = settingsDataStore.telegramUser.first()
            if (savedUserStr != null) {
                val parts = savedUserStr.split("|")
                if (parts.size >= 5) {
                    val id = parts[0].toLongOrNull() ?: 0L
                    _currentUser.value = TelegramUser(
                        id = id,
                        firstName = parts[1],
                        lastName = parts[2].ifEmpty { null },
                        username = parts[3].ifEmpty { null },
                        phoneNumber = parts[4],
                        profilePhoto = null
                    )
                }
            }
        }
    }

    override suspend fun connect() {
        if (_connectionState.value == TelegramConnectionState.CONNECTING ||
            _connectionState.value == TelegramConnectionState.AUTHORIZED
        ) {
            return
        }

        _connectionState.value = TelegramConnectionState.CONNECTING
        _error.value = null

        withContext(Dispatchers.Default) {
            val loadState = TdLibNativeLoader.load()
            if (loadState != TdLibNativeLoader.State.LOADED) {
                _connectionState.value = TelegramConnectionState.DISCONNECTED
                _error.value = TelegramError.TdLibRuntimeUnavailable
                DiagnosticsManager.log(
                    category = DiagnosticCategory.TELEGRAM_INIT,
                    severity = DiagnosticSeverity.WARN,
                    message = "Telegram integration unavailable: TDLib native binaries (libtdjni.so) not loaded (${loadState.name}).",
                    errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
                )
                return@withContext
            }

            try {
                if (tdLibClient == null) {
                    val apiIdInt = BuildConfig.TELEGRAM_API_ID.toIntOrNull() ?: 0
                    val client = TdLibClient(
                        context = context,
                        apiId = apiIdInt,
                        apiHash = BuildConfig.TELEGRAM_API_HASH,
                        applicationVersion = BuildConfig.VERSION_NAME
                    )
                    tdLibClient = client

                    scope.launch {
                        client.authorizationState.collect { state ->
                            handleAuthState(state)
                        }
                    }

                    client.start()
                }
            } catch (e: Throwable) {
                _connectionState.value = TelegramConnectionState.DISCONNECTED
                _error.value = TelegramError.Unknown(e.message ?: "Failed to initialize TDLib")
                DiagnosticsManager.log(
                    category = DiagnosticCategory.TELEGRAM_INIT,
                    severity = DiagnosticSeverity.ERROR,
                    message = "Fatal error initializing TDLib client: ${e.message}",
                    errorCode = ErrorCode.TELEGRAM_UNAVAILABLE,
                    exception = e
                )
            }
        }
    }

    private suspend fun handleAuthState(state: TdLibClient.AuthState) {
        when (state) {
            is TdLibClient.AuthState.Starting -> {
                _connectionState.value = TelegramConnectionState.CONNECTING
            }
            is TdLibClient.AuthState.ParametersRequired -> {
                _connectionState.value = TelegramConnectionState.CONNECTING
            }
            is TdLibClient.AuthState.PhoneNumberRequired -> {
                _connectionState.value = TelegramConnectionState.WAITING_FOR_PHONE
            }
            is TdLibClient.AuthState.CodeRequired -> {
                _connectionState.value = TelegramConnectionState.WAITING_FOR_CODE
            }
            is TdLibClient.AuthState.PasswordRequired -> {
                _connectionState.value = TelegramConnectionState.WAITING_FOR_PASSWORD
            }
            is TdLibClient.AuthState.Ready -> {
                _connectionState.value = TelegramConnectionState.AUTHORIZED
                fetchCurrentUser()
            }
            is TdLibClient.AuthState.LoggingOut -> {
                _connectionState.value = TelegramConnectionState.CLOSING
            }
            is TdLibClient.AuthState.Closing -> {
                _connectionState.value = TelegramConnectionState.CLOSING
            }
            is TdLibClient.AuthState.Closed -> {
                _connectionState.value = TelegramConnectionState.DISCONNECTED
            }
            is TdLibClient.AuthState.Failed -> {
                _connectionState.value = TelegramConnectionState.DISCONNECTED
                _error.value = TelegramError.TdLibRuntimeUnavailable
            }
        }
    }

    private suspend fun fetchCurrentUser() {
        val client = tdLibClient ?: return
        try {
            val user = client.getMe()
            if (user != null) {
                val telegramUser = TelegramUser(
                    id = user.id,
                    firstName = user.firstName ?: "",
                    lastName = user.lastName,
                    username = user.username,
                    phoneNumber = user.phoneNumber ?: "",
                    profilePhoto = null
                )
                _currentUser.value = telegramUser
                settingsDataStore.saveTelegramUser(
                    id = telegramUser.id,
                    firstName = telegramUser.firstName,
                    lastName = telegramUser.lastName,
                    username = telegramUser.username,
                    phone = telegramUser.phoneNumber
                )
                settingsDataStore.setTelegramConnectionState(TelegramConnectionState.AUTHORIZED.name)
            }
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_AUTH_STATE,
                severity = DiagnosticSeverity.WARN,
                message = "Failed to fetch user profile: ${e.message}"
            )
        }
    }

    override suspend fun sendPhoneNumber(phoneNumber: String) {
        val client = tdLibClient
        if (client == null || !TdLibNativeLoader.isLoaded) {
            _error.value = TelegramError.TdLibRuntimeUnavailable
            return
        }

        try {
            client.submitPhoneNumber(phoneNumber)
        } catch (e: TdLibClient.TdLibException) {
            val err = mapTdLibError(e.code, e.message)
            _error.value = err
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_AUTH_ERROR,
                severity = DiagnosticSeverity.ERROR,
                message = "Phone authentication failed [code ${e.code}]: ${e.message}",
                errorCode = ErrorCode.TELEGRAM_AUTH_FAILED
            )
        } catch (e: Exception) {
            _error.value = TelegramError.Unknown(e.message ?: "Authentication failed")
        }
    }

    override suspend fun sendCode(code: String) {
        val client = tdLibClient
        if (client == null || !TdLibNativeLoader.isLoaded) {
            _error.value = TelegramError.TdLibRuntimeUnavailable
            return
        }

        try {
            client.submitCode(code)
        } catch (e: TdLibClient.TdLibException) {
            val err = mapTdLibError(e.code, e.message)
            _error.value = err
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_AUTH_ERROR,
                severity = DiagnosticSeverity.ERROR,
                message = "Code verification failed [code ${e.code}]: ${e.message}",
                errorCode = ErrorCode.TELEGRAM_AUTH_FAILED
            )
        } catch (e: Exception) {
            _error.value = TelegramError.Unknown(e.message ?: "Code verification failed")
        }
    }

    override suspend fun sendPassword(password: String) {
        val client = tdLibClient
        if (client == null || !TdLibNativeLoader.isLoaded) {
            _error.value = TelegramError.TdLibRuntimeUnavailable
            return
        }

        try {
            client.submitPassword(password.toCharArray())
        } catch (e: TdLibClient.TdLibException) {
            val err = mapTdLibError(e.code, e.message)
            _error.value = err
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_AUTH_ERROR,
                severity = DiagnosticSeverity.ERROR,
                message = "2FA verification failed [code ${e.code}]: ${e.message}",
                errorCode = ErrorCode.TELEGRAM_AUTH_FAILED
            )
        } catch (e: Exception) {
            _error.value = TelegramError.Unknown(e.message ?: "Password verification failed")
        }
    }

    override suspend fun logout() {
        _connectionState.value = TelegramConnectionState.CLOSING
        withContext(Dispatchers.Default) {
            tdLibClient?.logOut()
            tdLibClient?.close()
            tdLibClient = null
            _currentUser.value = null
            _connectionState.value = TelegramConnectionState.DISCONNECTED
            _error.value = null
            settingsDataStore.clearTelegramUser()
            settingsDataStore.setTelegramConnectionState(TelegramConnectionState.DISCONNECTED.name)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_AUTH_STATE,
                severity = DiagnosticSeverity.INFO,
                message = "Telegram session reset."
            )
        }
    }

    override fun clearError() {
        _error.value = null
    }

    override fun getDestinations(query: String): Flow<List<TelegramDestination>> = flow {
        val client = tdLibClient
        if (client == null || _connectionState.value != TelegramConnectionState.AUTHORIZED) {
            emit(emptyList())
            return@flow
        }

        try {
            val chatIds = if (query.isBlank()) {
                client.getChats(limit = 100)
            } else {
                client.searchChats(query = query, limit = 50)
            }

            var myUserId: Long = 0L
            val meObj = client.getMe()
            if (meObj != null) {
                myUserId = meObj.id
            }

            val destinationList = mutableListOf<TelegramDestination>()
            for (chatId in chatIds) {
                val chat = client.getChat(chatId) ?: continue
                val id = chat.id
                var title = chat.title ?: "Chat $id"
                val chatType = chat.type
                val permissions = chat.permissions

                var destType = TelegramDestinationType.OTHER
                var canSend = true

                when (chatType) {
                    is TdApi.ChatTypePrivate -> {
                        destType = TelegramDestinationType.USER
                        if (id == myUserId) {
                            title = "Saved Messages"
                        }
                    }
                    is TdApi.ChatTypeBasicGroup -> {
                        destType = TelegramDestinationType.GROUP
                    }
                    is TdApi.ChatTypeSupergroup -> {
                        destType = if (chatType.isChannel) TelegramDestinationType.CHANNEL else TelegramDestinationType.SUPERGROUP
                    }
                }

                if (permissions != null) {
                    canSend = permissions.canSendOtherMessages || permissions.canSendMessages
                }

                destinationList.add(
                    TelegramDestination(
                        id = id,
                        title = title,
                        username = null,
                        type = destType,
                        photo = null,
                        canSendMessages = canSend
                    )
                )
            }

            DiagnosticsManager.log(
                category = DiagnosticCategory.DESTINATION_RESOLUTION,
                severity = DiagnosticSeverity.INFO,
                message = "Resolved ${destinationList.size} destinations from TDLib (Query: '$query')."
            )
            emit(destinationList)
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.DESTINATION_RESOLUTION,
                severity = DiagnosticSeverity.ERROR,
                message = "Failed to resolve destinations: ${e.message}",
                errorCode = ErrorCode.DESTINATION_UNAVAILABLE
            )
            emit(emptyList())
        }
    }

    private fun mapTdLibError(code: Int, message: String): TelegramError {
        val lower = message.lowercase()
        return when {
            lower.contains("phone_number_invalid") || lower.contains("phone number") -> TelegramError.InvalidPhoneNumber
            lower.contains("phone_code_invalid") || lower.contains("code") -> TelegramError.InvalidCode
            lower.contains("password_hash_invalid") || lower.contains("password") -> TelegramError.InvalidPassword
            lower.contains("flood_wait") || lower.contains("too many") -> TelegramError.RateLimited
            code == 401 || lower.contains("session") -> TelegramError.SessionExpired
            else -> TelegramError.Unknown(message)
        }
    }
}
