package com.telegramdrive.uploader.data.telegram.client

import android.content.Context
import android.os.Build
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TdLibClient(
    context: Context,
    private val apiId: Int,
    private val apiHash: String,
    private val applicationVersion: String = "1.0"
) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val databaseDirectory = File(appContext.filesDir, "tdlib/database")
    private val filesDirectory = File(appContext.filesDir, "tdlib/files")

    private val _authorizationState = MutableStateFlow<AuthState>(AuthState.Starting)
    val authorizationState: StateFlow<AuthState> = _authorizationState.asStateFlow()

    private val _updates = MutableSharedFlow<TdApi.Object>(
        replay = 0,
        extraBufferCapacity = 128
    )
    val updates: SharedFlow<TdApi.Object> = _updates.asSharedFlow()

    private val _errors = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 32
    )
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    private var clientInstance: Client? = null
    private var closed = false

    sealed interface AuthState {
        data object Starting : AuthState
        data object ParametersRequired : AuthState
        data object PhoneNumberRequired : AuthState
        data object CodeRequired : AuthState
        data class PasswordRequired(val hint: String) : AuthState
        data object Ready : AuthState
        data object LoggingOut : AuthState
        data object Closing : AuthState
        data object Closed : AuthState
        data class Failed(val message: String) : AuthState
    }

    fun start() {
        check(clientInstance == null) { "TDLib client has already been started" }
        check(!closed) { "TDLib client has been closed" }

        val loadState = TdLibNativeLoader.load()
        if (loadState != TdLibNativeLoader.State.LOADED) {
            val errorMsg = "TDLib native library unavailable (${loadState.name}): ${TdLibNativeLoader.lastError ?: "libtdjni.so not loaded"}"
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = errorMsg,
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            _authorizationState.value = AuthState.Failed(errorMsg)
            return
        }

        try {
            databaseDirectory.mkdirs()
            filesDirectory.mkdirs()

            val updateHandler = Client.ResultHandler { obj ->
                if (obj != null) {
                    handleObject(obj)
                }
            }

            val errorHandler = Client.ExceptionHandler { e ->
                DiagnosticsManager.log(
                    category = DiagnosticCategory.TELEGRAM_AUTH_ERROR,
                    severity = DiagnosticSeverity.ERROR,
                    message = "TDLib client unhandled exception: ${e?.message}",
                    errorCode = ErrorCode.TELEGRAM_UNAVAILABLE,
                    exception = e
                )
                scope.launch {
                    _errors.emit(e?.message ?: "Unknown TDLib internal error")
                }
            }

            clientInstance = Client.create(updateHandler, errorHandler, null)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.INFO,
                message = "Client.create executed successfully."
            )
        } catch (e: Throwable) {
            val errorMsg = "Fatal failure creating TDLib client: ${e.message}"
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.ERROR,
                message = errorMsg,
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE,
                exception = e
            )
            _authorizationState.value = AuthState.Failed(errorMsg)
        }
    }

    private fun handleObject(obj: TdApi.Object) {
        when (obj) {
            is TdApi.UpdateAuthorizationState -> {
                handleAuthorizationState(obj.authorizationState)
            }
            else -> {
                scope.launch {
                    _updates.emit(obj)
                }
            }
        }
    }

    private fun handleAuthorizationState(stateObj: TdApi.AuthorizationState) {
        when (stateObj) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                _authorizationState.value = AuthState.ParametersRequired
                scope.launch {
                    setParameters()
                }
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _authorizationState.value = AuthState.PhoneNumberRequired
            }
            is TdApi.AuthorizationStateWaitCode -> {
                _authorizationState.value = AuthState.CodeRequired
            }
            is TdApi.AuthorizationStateWaitPassword -> {
                _authorizationState.value = AuthState.PasswordRequired(stateObj.passwordHint ?: "")
            }
            is TdApi.AuthorizationStateReady -> {
                _authorizationState.value = AuthState.Ready
            }
            is TdApi.AuthorizationStateLoggingOut -> {
                _authorizationState.value = AuthState.LoggingOut
            }
            is TdApi.AuthorizationStateClosing -> {
                _authorizationState.value = AuthState.Closing
            }
            is TdApi.AuthorizationStateClosed -> {
                _authorizationState.value = AuthState.Closed
            }
            else -> {
                DiagnosticsManager.log(
                    category = DiagnosticCategory.TELEGRAM_AUTH_STATE,
                    severity = DiagnosticSeverity.INFO,
                    message = "Unhandled TDLib authorization state: ${stateObj::class.java.simpleName}"
                )
            }
        }
    }

    private suspend fun setParameters() {
        try {
            val parameters = TdApi.TdlibParameters().apply {
                useTestDc = false
                databaseDirectory = this@TdLibClient.databaseDirectory.absolutePath
                filesDirectory = this@TdLibClient.filesDirectory.absolutePath
                useFileDatabase = true
                useChatInfoDatabase = true
                useMessageDatabase = true
                useSecretChats = false
                apiId = this@TdLibClient.apiId
                apiHash = this@TdLibClient.apiHash
                systemLanguageCode = "en"
                deviceModel = Build.MODEL ?: "Android"
                systemVersion = Build.VERSION.RELEASE ?: "Unknown"
                applicationVersion = this@TdLibClient.applicationVersion
                enableStorageOptimizer = true
                ignoreFileNames = false
            }

            val request: TdApi.Function = TdApi.SetTdlibParameters(parameters)
            sendAndAwait(request)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.INFO,
                message = "TdApi.SetTdlibParameters sent and accepted."
            )
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.ERROR,
                message = "TDLIB_API_INVOCATION_FAILED: Failed to set TDLib parameters: ${e.message}",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE,
                exception = e
            )
            _authorizationState.value = AuthState.Failed("Failed to initialize TDLib parameters: ${e.message}")
        }
    }

    suspend fun submitPhoneNumber(phoneNumber: String) {
        val cleanPhone = phoneNumber.trim()
        val request: TdApi.Function = TdApi.SetAuthenticationPhoneNumber(
            cleanPhone,
            TdApi.PhoneNumberAuthenticationSettings(false, false, false, false, null)
        )
        sendAndAwait(request)
    }

    suspend fun submitCode(code: String) {
        val cleanCode = code.trim()
        val request: TdApi.Function = TdApi.CheckAuthenticationCode(cleanCode)
        sendAndAwait(request)
    }

    suspend fun submitPassword(password: CharArray) {
        require(password.isNotEmpty()) { "Password must not be empty" }
        try {
            val request: TdApi.Function = TdApi.CheckAuthenticationPassword(String(password))
            sendAndAwait(request)
        } finally {
            password.fill('\u0000')
        }
    }

    suspend fun getChats(limit: Int = 100): LongArray {
        return try {
            val request: TdApi.Function = TdApi.GetChats(null, limit)
            val result = sendAndAwait(request)
            if (result is TdApi.Chats) {
                result.chatIds
            } else {
                LongArray(0)
            }
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLIB_API_INVOCATION_FAILED: getChats failed: ${e.message}"
            )
            LongArray(0)
        }
    }

    suspend fun searchChats(query: String, limit: Int = 50): LongArray {
        return try {
            val request: TdApi.Function = TdApi.SearchChats(query, limit)
            val result = sendAndAwait(request)
            if (result is TdApi.Chats) {
                result.chatIds
            } else {
                LongArray(0)
            }
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLIB_API_INVOCATION_FAILED: searchChats failed: ${e.message}"
            )
            LongArray(0)
        }
    }

    suspend fun getChat(chatId: Long): TdApi.Chat? {
        return try {
            val request: TdApi.Function = TdApi.GetChat(chatId)
            val result = sendAndAwait(request)
            result as? TdApi.Chat
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLIB_API_INVOCATION_FAILED: getChat($chatId) failed: ${e.message}"
            )
            null
        }
    }

    suspend fun getMe(): TdApi.User? {
        return try {
            val request: TdApi.Function = TdApi.GetMe()
            val result = sendAndAwait(request)
            result as? TdApi.User
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLIB_API_INVOCATION_FAILED: getMe failed: ${e.message}"
            )
            null
        }
    }

    suspend fun sendDocumentOrVideoMessage(
        chatId: Long,
        localFilePath: String,
        mimeType: String,
        caption: String = "",
        isVideo: Boolean = false,
        width: Int = 0,
        height: Int = 0,
        duration: Int = 0
    ): TdApi.Message? {
        return try {
            val inputFile = TdApi.InputFileLocal(localFilePath)
            val formattedText = if (caption.isNotBlank()) {
                TdApi.FormattedText(caption, emptyArray())
            } else {
                null
            }

            val inputMessageContent: TdApi.InputMessageContent = if (isVideo && mimeType.startsWith("video/")) {
                TdApi.InputMessageVideo(
                    inputFile,
                    null,
                    IntArray(0),
                    duration,
                    width,
                    height,
                    true,
                    formattedText,
                    0
                )
            } else {
                TdApi.InputMessageDocument(
                    inputFile,
                    null,
                    false,
                    formattedText
                )
            }

            val request: TdApi.Function = TdApi.SendMessage(
                chatId,
                0L,
                0L,
                null,
                null,
                inputMessageContent
            )

            val result = sendAndAwait(request)
            result as? TdApi.Message
        } catch (e: Exception) {
            DiagnosticsManager.log(
                category = DiagnosticCategory.UPLOAD_FAILED,
                severity = DiagnosticSeverity.ERROR,
                message = "TDLIB_API_INVOCATION_FAILED: SendMessage failed: ${e.message}"
            )
            null
        }
    }

    suspend fun sendAndAwait(functionObj: TdApi.Function): TdApi.Object {
        check(!closed) { "TDLib client is closed" }
        val activeClient = checkNotNull(clientInstance) { "TDLib client has not been started" }

        return suspendCancellableCoroutine { continuation ->
            activeClient.send(functionObj) { result ->
                if (result is TdApi.Error) {
                    continuation.resumeWithException(TdLibException(result.code, result.message))
                } else if (result != null) {
                    continuation.resume(result)
                } else {
                    continuation.resumeWithException(TdLibException(-1, "Null response received from TDLib"))
                }
            }
        }
    }

    fun logOut() {
        if (!closed && _authorizationState.value == AuthState.Ready) {
            clientInstance?.send(TdApi.LogOut(), null)
        }
    }

    fun close() {
        if (closed) return
        closed = true
        try {
            clientInstance?.send(TdApi.Close(), null)
        } catch (_: Exception) {}
        scope.cancel()
        clientInstance = null
    }

    class TdLibException(val code: Int, override val message: String) : Exception(message)
}
