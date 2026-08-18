package com.telegramdrive.uploader.core.diagnostics

import android.content.Context
import android.os.Build
import java.io.File

object TdLibNativeDiagnostics {

    data class RuntimeDiagnosticReport(
        val javaBindingsPresent: Boolean,
        val nativeLibraryPresent: Boolean,
        val nativeLoadSuccess: Boolean,
        val nativeLoadError: String?,
        val deviceSupportedAbis: List<String>,
        val activeAbi: String,
        val nativeAbiMatch: Boolean,
        val tdlibVersion: String
    )

    private var nativeLoaded = false
    private var nativeLoadErrorMessage: String? = null

    init {
        tryLoadNative()
    }

    @Synchronized
    fun tryLoadNative(): Boolean {
        if (nativeLoaded) return true

        return try {
            System.loadLibrary("tdjni")
            nativeLoaded = true
            nativeLoadErrorMessage = null
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.INFO,
                message = "TDLib native library (libtdjni.so) loaded successfully."
            )
            true
        } catch (e: UnsatisfiedLinkError) {
            nativeLoaded = false
            nativeLoadErrorMessage = "UnsatisfiedLinkError: ${e.message}"
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.WARN,
                message = "TDLib native library load failed (expected if .so not packaged): ${e.message}",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            false
        } catch (e: Throwable) {
            nativeLoaded = false
            nativeLoadErrorMessage = "${e::class.java.simpleName}: ${e.message}"
            DiagnosticsManager.log(
                category = DiagnosticCategory.TELEGRAM_INIT,
                severity = DiagnosticSeverity.ERROR,
                message = "Unexpected error loading TDLib native runtime: ${e.message}",
                errorCode = ErrorCode.TELEGRAM_UNAVAILABLE
            )
            false
        }
    }

    fun isNativeLoaded(): Boolean = nativeLoaded

    fun getDiagnosticReport(context: Context): RuntimeDiagnosticReport {
        val supportedAbis = Build.SUPPORTED_ABIS.toList()
        val activeAbi = supportedAbis.firstOrNull() ?: "unknown"

        val hasJavaBindings = runCatching {
            Class.forName("org.drinkless.tdlib.Client")
            Class.forName("org.drinkless.tdlib.TdApi")
            Class.forName("org.drinkless.tdlib.Log")
            true
        }.getOrDefault(false)

        val nativeDir = File(context.applicationInfo.nativeLibraryDir)
        val tdjniFile = File(nativeDir, "libtdjni.so")
        val isNativePresent = tdjniFile.exists() && tdjniFile.length() > 0

        val isAbiSupported = supportedAbis.any { abi ->
            abi == "arm64-v8a" || abi == "armeabi-v7a" || abi == "x86_64"
        }

        return RuntimeDiagnosticReport(
            javaBindingsPresent = hasJavaBindings,
            nativeLibraryPresent = isNativePresent,
            nativeLoadSuccess = nativeLoaded,
            nativeLoadError = nativeLoadErrorMessage,
            deviceSupportedAbis = supportedAbis,
            activeAbi = activeAbi,
            nativeAbiMatch = isAbiSupported && isNativePresent,
            tdlibVersion = "1.8.0"
        )
    }
}
