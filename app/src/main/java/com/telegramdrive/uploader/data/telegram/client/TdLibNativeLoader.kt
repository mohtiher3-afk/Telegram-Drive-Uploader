package com.telegramdrive.uploader.data.telegram.client

import android.os.Build
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.ErrorCode
import java.util.concurrent.atomic.AtomicReference

object TdLibNativeLoader {

    enum class State {
        NOT_ATTEMPTED,
        LOADING,
        LOADED,
        UNAVAILABLE,
        FAILED
    }

    private val currentState = AtomicReference(State.NOT_ATTEMPTED)
    private var failureMessage: String? = null
    private const val LIBRARY_NAME = "tdjni"

    val state: State
        get() = currentState.get()

    val lastError: String?
        get() = failureMessage

    val isLoaded: Boolean
        get() = currentState.get() == State.LOADED

    fun getActiveAbi(): String {
        return Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
    }

    @Synchronized
    fun load(): State {
        val existing = currentState.get()
        if (existing == State.LOADED || existing == State.UNAVAILABLE || existing == State.FAILED) {
            return existing
        }

        currentState.set(State.LOADING)
        val activeAbi = getActiveAbi()

        DiagnosticsManager.log(
            category = DiagnosticCategory.TELEGRAM_INIT,
            severity = DiagnosticSeverity.INFO,
            message = "TDLIB_NATIVE_LOAD_START: Attempting to load native library '$LIBRARY_NAME' for ABI '$activeAbi'."
        )

        val supported = Build.SUPPORTED_ABIS.any { abi ->
            abi == "arm64-v8a" || abi == "armeabi-v7a" || abi == "x86_64"
        }

        if (!supported) {
            failureMessage = "TDLIB_ABI_UNAVAILABLE: Unsupported device ABI '$activeAbi'."
            currentState.set(State.UNAVAILABLE)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.ERROR,
                message = "TDLIB_NATIVE_LOAD_FAILED: $failureMessage",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            return State.UNAVAILABLE
        }

        return try {
            System.loadLibrary(LIBRARY_NAME)
            currentState.set(State.LOADED)
            failureMessage = null
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.INFO,
                message = "TDLIB_NATIVE_LOAD_SUCCESS: '$LIBRARY_NAME' loaded successfully."
            )
            State.LOADED
        } catch (e: UnsatisfiedLinkError) {
            failureMessage = "UnsatisfiedLinkError: ${e.message}"
            currentState.set(State.UNAVAILABLE)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLIB_NATIVE_LOAD_FAILED (Native binary not packaged): ${e.message}",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            State.UNAVAILABLE
        } catch (e: Throwable) {
            failureMessage = "${e::class.java.simpleName}: ${e.message}"
            currentState.set(State.FAILED)
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.ERROR,
                message = "TDLIB_NATIVE_LOAD_FAILED (Fatal native loading exception): ${e.message}",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            State.FAILED
        }
    }
}
