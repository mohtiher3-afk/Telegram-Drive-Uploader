package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
import android.os.Build
import com.telegramdrive.uploader.BuildConfig
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
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
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
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
import java.util.concurrent.ConcurrentHashMap
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
    private val supergroups = LinkedHashMap<Long, TdApi.Supergroup>()
    private val _chatDestinations = MutableStateFlow<List<TelegramDestination>>(emptyList())
    private val chatsRequested = AtomicBoolean(false)
    private data class PendingUpload(
        val channel: SendChannel<TelegramUploadEvent>,
        val destinationId: Long,
        val totalBytes: Long,
        val provisionalMessageId: Long? = null
    )

    private val pendingUploads = ConcurrentHashMap<Int, PendingUpload>()
    private val pendingMessageSuccesses = ConcurrentHashMap<Long, TdApi.UpdateMessageSendSucceeded>()
    private val pendingMessageFailures = ConcurrentHashMap<Long, TdApi.UpdateMessageSendFailed>()
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
            synchronized(chatLock) {
                chats.clear()
                supergroups.clear()
            }
            _chatDestinations.value = emptyList()
            chatsRequested.set(false)
            _connectionState.value = TelegramConnectionState.DISCONNECTED
            settingsDataStore.clearTelegramSession()
        }
    }

    override fun clearError() {
        _error.value = null
    }

    override fun getDestinations(query: String): Flow<List<TelegramDestination>> {
        requestDestinationSearch(query)
        return _chatDestinations.map { destinations ->
            val normalized = query.trim().lowercase(Locale.US).removePrefix("@")
            if (normalized.isBlank()) destinations
            else destinations.filter { destination ->
                destination.title.lowercase(Locale.US).contains(normalized) ||
                    destination.username?.lowercase(Locale.US)?.contains(normalized) == true
            }
        }.distinctUntilChanged()
    }

    private fun requestDestinationSearch(query: String) {
        val normalized = query.trim()
        if (normalized.isBlank() || _connectionState.value != TelegramConnectionState.AUTHORIZED) return
        val client = tdClient ?: return
        val searchText = normalized.removePrefix("@").trim()
        if (searchText.isBlank()) return
        // SearchPublicChat resolves an exact public username; SearchChatsOnServer handles names and partial matches.
        client.send(TdApi.SearchPublicChat(searchText), { result -> handleDestinationSearchResult(result) }, null)
        client.send(
            TdApi.SearchChatsOnServer(searchText, null, 100),
            { result -> handleDestinationSearchResult(result) },
            null
        )
    }

    private fun handleDestinationSearchResult(result: TdApi.Object) {
        when (result) {
            is TdApi.Chat -> upsertChat(result)
            is TdApi.Chats -> result.chatIds.forEach(::requestChat)
            // A not-found username or empty server result is normal search behavior, not an auth failure.
            is TdApi.Error -> Unit
        }
    }

    override fun uploadLocalDocument(task: com.telegramdrive.uploader.domain.model.UploadTask, localPath: String): Flow<TelegramUploadEvent> = callbackFlow {
        val client = tdClient
        if (client == null || _connectionState.value != TelegramConnectionState.AUTHORIZED) {
            trySend(TelegramUploadEvent.Failed("Telegram account is not authorized", true))
            close()
            return@callbackFlow
        }

        val fileType = if (task.mimeType.startsWith("video/")) {
            TdApi.FileTypeVideo()
        } else {
            TdApi.FileTypeDocument()
        }
        val input = TdApi.InputFileLocal(localPath)
        client.send(
            TdApi.PreliminaryUploadFile(input, fileType, 32),
            { result ->
                if (result !is TdApi.File) {
                    trySend(TelegramUploadEvent.Failed("TDLib did not return an upload file", false))
                    close()
                } else {
                    val fileId = result.id
                    pendingUploads[fileId] = PendingUpload(
                        channel = this@callbackFlow,
                        destinationId = task.destinationId,
                        totalBytes = task.fileSize.coerceAtLeast(result.size.toLong())
                    )
                    trySend(TelegramUploadEvent.Progress(result.remote.uploadedSize, result.size.coerceAtLeast(task.fileSize)))
                    val content = buildUploadMessageContent(task, fileId)
                    val contentType = if (content is TdApi.InputMessageVideo) "video" else "document"
                    DiagnosticsManager.log(
                        category = DiagnosticCategory.UPLOAD_STARTED,
                        severity = DiagnosticSeverity.INFO,
                        message = "Handing off $contentType message to Telegram TDLib client for delivery.",
                        uploadId = task.id
                    )
                    client.send(
                        TdApi.SendMessage(task.destinationId, null, null, null, null, content),
                        { sent ->
                            if (sent is TdApi.Message) {
                                // SendMessage returns a local/pending Message first. It is not proof
                                // that Telegram accepted the message. Wait for UpdateMessageSendSucceeded.
                                pendingUploads.computeIfPresent(fileId) { _, pending ->
                                    pending.copy(provisionalMessageId = sent.id)
                                }
                                pendingMessageSuccesses.remove(sent.id)?.let(::handleMessageSendSucceeded)
                                pendingMessageFailures.remove(sent.id)?.let(::handleMessageSendFailed)
                            } else if (sent is TdApi.Error) {
                                trySend(TelegramUploadEvent.Failed(sent.message, isRetryableTelegramError(sent.code)))
                                pendingUploads.remove(fileId)
                                close()
                            }
                        },
                        { failure ->
                            trySend(TelegramUploadEvent.Failed(failure.message ?: "TDLib send failed", true))
                            pendingUploads.remove(fileId)
                            close()
                        }
                    )
                }
            },
            { failure ->
                trySend(TelegramUploadEvent.Failed(failure.message ?: "TDLib upload failed", true))
                close()
            }
        )
        awaitClose { pendingUploads.entries.removeIf { it.value.channel == this@callbackFlow } }
    }

    private fun isRetryableTelegramError(code: Int): Boolean = code == 420 || code == 429 || code >= 500

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
            is TdApi.UpdateFile -> handleFileUpdate(objectValue)
            is TdApi.UpdateMessageSendSucceeded -> handleMessageSendSucceeded(objectValue)
            is TdApi.UpdateMessageSendFailed -> handleMessageSendFailed(objectValue)
            is TdApi.UpdateChatTitle -> updateChatTitle(objectValue)
            is TdApi.UpdateChatPermissions -> updateChatPermissions(objectValue)
            is TdApi.UpdateSupergroup -> upsertSupergroup(objectValue.supergroup)
            is TdApi.Chats -> objectValue.chatIds.forEach(::requestChat)
            is TdApi.Chat -> upsertChat(objectValue)
            is TdApi.Supergroup -> upsertSupergroup(objectValue)
            is TdApi.User -> handleAuthenticatedUser(objectValue)
            is TdApi.Error -> mapError(objectValue)
        }
    }

    private fun handleFileUpdate(update: TdApi.UpdateFile) {
        val channel = pendingUploads[update.file.id] ?: return
        val total = update.file.size.coerceAtLeast(update.file.expectedSize)
        channel.channel.trySend(TelegramUploadEvent.Progress(update.file.remote.uploadedSize, total))
    }

    private fun handleMessageSendSucceeded(update: TdApi.UpdateMessageSendSucceeded) {
        val match = pendingUploads.entries.firstOrNull { (_, pending) ->
            pending.destinationId == update.message.chatId &&
                pending.provisionalMessageId != null && pending.provisionalMessageId == update.oldMessageId
        } ?: run {
            if (update.oldMessageId != 0L) pendingMessageSuccesses[update.oldMessageId] = update
            return
        }
        val pending = pendingUploads.remove(match.key) ?: return
        pending.channel.trySend(TelegramUploadEvent.Progress(pending.totalBytes, pending.totalBytes))
        pending.channel.trySend(TelegramUploadEvent.Completed)
        pending.channel.close()
    }

    private fun handleMessageSendFailed(update: TdApi.UpdateMessageSendFailed) {
        val match = pendingUploads.entries.firstOrNull { (_, pending) ->
            pending.destinationId == update.message.chatId &&
                pending.provisionalMessageId != null && pending.provisionalMessageId == update.oldMessageId
        } ?: run {
            if (update.oldMessageId != 0L) pendingMessageFailures[update.oldMessageId] = update
            return
        }
        val pending = pendingUploads.remove(match.key) ?: return
        pending.channel.trySend(
            TelegramUploadEvent.Failed(update.error.message, isRetryableTelegramError(update.error.code))
        )
        pending.channel.close()
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
            TdApi.GetChats(TdApi.ChatListMain(), 500),
            { result -> handleTdLibObject(result) },
            null
        )
    }

    private fun requestChat(chatId: Long) {
        tdClient?.send(TdApi.GetChat(chatId), { result ->
            when (result) {
                is TdApi.Chat -> upsertChat(result)
                is TdApi.Error -> recordDestinationLookupFailure("chat", chatId, result)
            }
        }, null)
    }

    private fun requestSupergroup(supergroupId: Long) {
        tdClient?.send(TdApi.GetSupergroup(supergroupId), { result ->
            when (result) {
                is TdApi.Supergroup -> upsertSupergroup(result)
                is TdApi.Error -> recordDestinationLookupFailure("supergroup", supergroupId, result)
            }
        }, null)
    }

    private fun recordDestinationLookupFailure(kind: String, id: Long, error: TdApi.Error) {
        // Destination metadata can disappear while Telegram refreshes chat state. These
        // lookup failures must not be routed through mapError(), which is reserved for
        // authentication and connection requests and would show a false auth failure.
        DiagnosticsManager.log(
            category = DiagnosticCategory.TELEGRAM_INIT,
            severity = DiagnosticSeverity.INFO,
            message = "Destination $kind lookup unavailable for id=$id: ${error.code}"
        )
    }

    private fun upsertChat(chat: TdApi.Chat) {
        if (chat.type is TdApi.ChatTypeSupergroup) {
            requestSupergroup((chat.type as TdApi.ChatTypeSupergroup).supergroupId)
        }
        synchronized(chatLock) { chats[chat.id] = chat }
        rebuildDestinations()
    }

    private fun upsertSupergroup(supergroup: TdApi.Supergroup) {
        synchronized(chatLock) { supergroups[supergroup.id] = supergroup }
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
                val supergroup = (chat.type as? TdApi.ChatTypeSupergroup)?.let { supergroups[it.supergroupId] }
                val username = supergroup?.usernames?.editableUsername
                    ?: supergroup?.usernames?.activeUsernames?.firstOrNull()
                val canSend = when {
                    chat.type is TdApi.ChatTypePrivate -> true
                    supergroup?.status is TdApi.ChatMemberStatusCreator -> true
                    supergroup?.status is TdApi.ChatMemberStatusAdministrator ->
                        (supergroup.status as TdApi.ChatMemberStatusAdministrator).rights?.canPostMessages == true
                    supergroup?.status is TdApi.ChatMemberStatusRestricted ->
                        (supergroup.status as TdApi.ChatMemberStatusRestricted).permissions?.canSendBasicMessages == true
                    else -> chat.permissions?.canSendBasicMessages == true || supergroup == null
                }
                if (!canSend) return@mapNotNull null
                TelegramDestination(
                    id = chat.id,
                    title = chat.title.ifBlank { "Telegram chat" },
                    username = username,
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
                // Determine absolute library path for robustness
                val libDir = context.applicationInfo.nativeLibraryDir
                android.util.Log.i("TelegramClient", "Loading native libraries from: $libDir")

                // Attempt loading bundled dependencies with absolute paths first
                val cryptoPath = "$libDir/libcrypto.so"
                val sslPath = "$libDir/libssl.so"
                val tdnjiPath = "$libDir/libtdjni.so"

                if (java.io.File(cryptoPath).exists()) {
                    runCatching { System.load(cryptoPath) }
                } else {
                    runCatching { System.loadLibrary("crypto") }
                }

                if (java.io.File(sslPath).exists()) {
                    runCatching { System.load(sslPath) }
                } else {
                    runCatching { System.loadLibrary("ssl") }
                }

                if (java.io.File(tdnjiPath).exists()) {
                    System.load(tdnjiPath)
                } else {
                    System.loadLibrary("tdjni")
                }

                android.util.Log.i("TelegramClient", "All TDLib native libraries loaded successfully")
            } catch (failure: Throwable) {
                nativeLoaded.set(false)
                android.util.Log.e("TelegramClient", "Native library load failure: ${failure.message}", failure)
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


internal fun buildUploadMessageContent(
    task: com.telegramdrive.uploader.domain.model.UploadTask,
    fileId: Int
): TdApi.InputMessageContent {
    val caption = TdApi.FormattedText(task.fileName, emptyArray())
    val isVideoMime = task.mimeType.startsWith("video/", ignoreCase = true)
    val hasValidMetadata = task.width > 0 && task.height > 0
    
    if (!isVideoMime || !hasValidMetadata) {
        return TdApi.InputMessageDocument(
            TdApi.InputDocument(TdApi.InputFileId(fileId), null, false),
            caption
        )
    }
    val durationSeconds = (task.duration / 1_000L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    return TdApi.InputMessageVideo(
        TdApi.InputVideo(
            TdApi.InputFileId(fileId),
            null,
            null,
            0,
            intArrayOf(),
            durationSeconds,
            task.width,
            task.height,
            true
        ),
        caption,
        false,
        null,
        false
    )
}
