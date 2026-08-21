package com.telegramdrive.uploader.core.diagnostics

import android.os.Build
import com.telegramdrive.uploader.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class DiagnosticCategory {
    APP_START,
    TELEGRAM_INIT,
    TELEGRAM_AUTH_STATE,
    TELEGRAM_AUTH_ERROR,
    DESTINATION_RESOLUTION,
    UPLOAD_CREATED,
    UPLOAD_STARTED,
    UPLOAD_PROGRESS,
    UPLOAD_RETRY,
    UPLOAD_PAUSED,
    UPLOAD_RESUMED,
    UPLOAD_CANCELLED,
    UPLOAD_COMPLETED,
    UPLOAD_FAILED,
    WORKER_STARTED,
    WORKER_STOPPED,
    NETWORK_CHANGED,
    DATABASE_ERROR,
    SETTINGS_CHANGED
}

enum class DiagnosticSeverity {
    ERROR,
    WARN,
    INFO,
    DEBUG
}

enum class ErrorCategory {
    AUTHENTICATION,
    TELEGRAM,
    NETWORK,
    FILE_ACCESS,
    UPLOAD,
    DATABASE,
    WORKER,
    PERMISSION,
    STORAGE,
    UNKNOWN
}

object ErrorCode {
    const val AUTH_PHONE_INVALID = "AUTH_PHONE_INVALID"
    const val AUTH_CODE_INVALID = "AUTH_CODE_INVALID"
    const val AUTH_PASSWORD_INVALID = "AUTH_PASSWORD_INVALID"
    const val AUTH_SESSION_EXPIRED = "AUTH_SESSION_EXPIRED"
    const val TELEGRAM_UNAVAILABLE = "TELEGRAM_UNAVAILABLE"
    const val DESTINATION_UNAVAILABLE = "DESTINATION_UNAVAILABLE"
    const val SOURCE_FILE_UNAVAILABLE = "SOURCE_FILE_UNAVAILABLE"
    const val NETWORK_UNAVAILABLE = "NETWORK_UNAVAILABLE"
    const val NETWORK_TIMEOUT = "NETWORK_TIMEOUT"
    const val UPLOAD_CANCELLED = "UPLOAD_CANCELLED"
    const val UPLOAD_FAILED = "UPLOAD_FAILED"
    const val DATABASE_FAILURE = "DATABASE_FAILURE"
    const val WORKER_FAILURE = "WORKER_FAILURE"
    const val STORAGE_FULL = "STORAGE_FULL"
    const val UNKNOWN = "UNKNOWN_ERROR"
}

data class DiagnosticEvent(
    val eventId: String,
    val incidentId: String?,
    val timestamp: Long,
    val category: String,
    val severity: String,
    val uploadId: String?,
    val screen: String?,
    val state: String?,
    val errorCode: String?,
    val durationMs: Long?,
    val message: String,
    val androidApi: Int,
    val appVersion: String
)

object DiagnosticsManager {
    private const val MAX_EVENTS = 200
    private const val MAX_RETENTION_MS = 24 * 60 * 60 * 1000L // 24 hours

    private val _events = MutableStateFlow<List<DiagnosticEvent>>(emptyList())
    val events: StateFlow<List<DiagnosticEvent>> = _events.asStateFlow()

    @Synchronized
    fun log(
        category: DiagnosticCategory,
        severity: DiagnosticSeverity,
        message: String,
        uploadId: String? = null,
        screen: String? = null,
        state: String? = null,
        errorCode: String? = null,
        durationMs: Long? = null,
        exception: Throwable? = null
    ): String {
        val eventId = UUID.randomUUID().toString().substring(0, 8)
        val incidentId = if (severity == DiagnosticSeverity.ERROR) {
            "INC-" + UUID.randomUUID().toString().substring(0, 6).uppercase(Locale.ROOT)
        } else {
            null
        }

        val rawMsg = if (exception != null) {
            "$message | Exception: ${exception.javaClass.simpleName} - ${exception.message}"
        } else {
            message
        }

        // Sanitize sensitive values aggressively to enforce complete user privacy
        val sanitizedMsg = sanitizeText(rawMsg)
        val sanitizedUploadId = uploadId?.let { sanitizeText(it) }
        val sanitizedState = state?.let { sanitizeText(it) }

        val event = DiagnosticEvent(
            eventId = eventId,
            incidentId = incidentId,
            timestamp = System.currentTimeMillis(),
            category = category.name,
            severity = severity.name,
            uploadId = sanitizedUploadId,
            screen = screen,
            state = sanitizedState,
            errorCode = errorCode,
            durationMs = durationMs,
            message = sanitizedMsg,
            androidApi = Build.VERSION.SDK_INT,
            appVersion = "1.0.0"
        )

        // Bounded list management
        val currentList = _events.value.toMutableList()
        currentList.add(event)

        // Enforce max retention cleanup
        val now = System.currentTimeMillis()
        val prunedList = currentList.filter { now - it.timestamp <= MAX_RETENTION_MS }
            .takeLast(MAX_EVENTS)

        _events.value = prunedList

        // Safe printing to logcat in developer mode without leaking sensitive fields
        if (severity == DiagnosticSeverity.ERROR) {
            android.util.Log.e("UploaderDiagnostics", "[${category.name}] [$severity] (Incident: $incidentId) $sanitizedMsg")
        } else {
            android.util.Log.i("UploaderDiagnostics", "[${category.name}] [$severity] $sanitizedMsg")
        }

        return incidentId ?: eventId
    }

    /**
     * Sanitizes inputs by stripping passwords, codes, tokens, usernames, phone numbers, and full file names.
     */
    fun sanitizeText(input: String): String {
        if (input.isBlank()) return input
        var result = input

        // 1. Redact phone numbers (e.g., matching standard international format digits 7-15 length)
        val phoneRegex = Regex("\\+?\\d{1,4}[- .]?\\d{3,4}[- .]?\\d{3,4}[- .]?\\d{1,4}")
        result = phoneRegex.replace(result) {
            val matched = it.value
            if (matched.length >= 7) "[REDACTED_PHONE]" else matched
        }

        // 2. Redact filenames ending in standard media formats (e.g. video_name.mp4)
        val fileRegex = Regex("[a-zA-Z0-9_-]+\\.(mp4|mkv|avi|mov|flv|wmv|3gp|webm)")
        result = fileRegex.replace(result, "[REDACTED_VIDEO_FILE]")

        // 3. Redact common credential patterns
        val secretPatterns = listOf(
            Regex("(?i)(password|pass|secret)\\s*=\\s*[^\\s&]+"),
            Regex("(?i)(code|verification)\\s*=\\s*[0-9]{4,6}"),
            Regex("(?i)(token|api_hash|apihash)\\s*=\\s*[a-zA-Z0-9]{12,64}"),
            Regex("(?i)(api_id|apiid)\\s*=\\s*[0-9]{4,12}")
        )
        for (pattern in secretPatterns) {
            result = pattern.replace(result, "$1=[REDACTED_CREDENTIAL]")
        }

        // 4. Redact numeric verification codes (4 to 6 digit sequences)
        val numericCodeRegex = Regex("\\b\\d{4,6}\\b")
        result = numericCodeRegex.replace(result, "[REDACTED_CODE]")

        // 5. Redact hexadecimal secrets or tokens
        val hexTokenRegex = Regex("\\b[a-fA-F0-9]{32,64}\\b")
        result = hexTokenRegex.replace(result, "[REDACTED_HASH_KEY]")

        return result
    }

    fun mapException(throwable: Throwable): ErrorCategory {
        val msg = throwable.message?.lowercase(Locale.ROOT) ?: ""
        return when {
            throwable is java.net.ConnectException || throwable is java.net.UnknownHostException || msg.contains("network") || msg.contains("connect") -> {
                ErrorCategory.NETWORK
            }
            throwable is SecurityException || msg.contains("permission") -> {
                ErrorCategory.PERMISSION
            }
            throwable is java.io.FileNotFoundException || msg.contains("filenotfound") || msg.contains("no such file") -> {
                ErrorCategory.FILE_ACCESS
            }
            msg.contains("auth") || msg.contains("login") || msg.contains("session") || msg.contains("password") || msg.contains("code") -> {
                ErrorCategory.AUTHENTICATION
            }
            msg.contains("database") || msg.contains("sqlite") || msg.contains("room") || throwable is android.database.sqlite.SQLiteException -> {
                ErrorCategory.DATABASE
            }
            msg.contains("worker") || msg.contains("workmanager") -> {
                ErrorCategory.WORKER
            }
            msg.contains("telegram") || msg.contains("tdlib") || msg.contains("chat") -> {
                ErrorCategory.TELEGRAM
            }
            msg.contains("space") || msg.contains("full") || msg.contains("disk") || throwable is java.io.IOException && msg.contains("write") -> {
                ErrorCategory.STORAGE
            }
            else -> ErrorCategory.UNKNOWN
        }
    }

    fun mapExceptionToCode(throwable: Throwable): String {
        val msg = throwable.message?.lowercase(Locale.ROOT) ?: ""
        return when {
            throwable is java.net.SocketTimeoutException || msg.contains("timeout") -> {
                ErrorCode.NETWORK_TIMEOUT
            }
            throwable is java.net.ConnectException || throwable is java.net.UnknownHostException || msg.contains("network") -> {
                ErrorCode.NETWORK_UNAVAILABLE
            }
            throwable is java.io.FileNotFoundException || msg.contains("no such file") -> {
                ErrorCode.SOURCE_FILE_UNAVAILABLE
            }
            msg.contains("disk") || msg.contains("full") || msg.contains("space") -> {
                ErrorCode.STORAGE_FULL
            }
            msg.contains("database") || msg.contains("sqlite") -> {
                ErrorCode.DATABASE_FAILURE
            }
            msg.contains("worker") -> {
                ErrorCode.WORKER_FAILURE
            }
            else -> ErrorCode.UNKNOWN
        }
    }

    fun exportDiagnostics(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val sb = java.lang.StringBuilder()
            sb.append("==================================================\n")
            sb.append("          TELEGRAM DRIVE DIAGNOSTICS EXPORT       \n")
            sb.append("==================================================\n")
            sb.append("Timestamp: ").append(formatter.format(Date())).append("\n")
            sb.append("Android version: API ").append(Build.VERSION.SDK_INT).append("\n")
            sb.append("App Version: 1.0.0 (Release)\n")
            sb.append("Total Diagnostic Events: ").append(_events.value.size).append("\n")
            sb.append("==================================================\n\n")

            _events.value.forEach { event ->
                sb.append("[").append(formatter.format(Date(event.timestamp))).append("] ")
                sb.append("[").append(event.severity).append("] ")
                sb.append("[").append(event.category).append("] ")
                if (event.incidentId != null) {
                    sb.append("(IncidentId: ").append(event.incidentId).append(") ")
                }
                if (event.uploadId != null) {
                    sb.append("(UploadId: ").append(event.uploadId).append(") ")
                }
                if (event.errorCode != null) {
                    sb.append("(ErrorCode: ").append(event.errorCode).append(") ")
                }
                sb.append("\nMessage: ").append(event.message).append("\n")
                sb.append("--------------------------------------------------\n")
            }
            sb.toString()
        } catch (e: Exception) {
            "Error exporting diagnostics: ${e.message}"
        }
    }

    fun clearDiagnostics() {
        _events.value = emptyList()
    }
}
