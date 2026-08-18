package com.telegramdrive.uploader

import android.app.Application
import android.content.ComponentCallbacks2
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TelegramDriveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DiagnosticsManager.log(
            category = DiagnosticCategory.APP_START,
            severity = DiagnosticSeverity.INFO,
            message = "Telegram Drive Uploader has successfully initialized in production environment."
        )
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Properly handle memory trim levels according to Android design guidelines
        val levelDescription = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "BACKGROUND_MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "BACKGROUND_COMPLETE"
            else -> "UNKNOWN"
        }
        
        DiagnosticsManager.log(
            category = DiagnosticCategory.SETTINGS_CHANGED,
            severity = DiagnosticSeverity.WARN,
            message = "OS memory pressure received. Trimming memory resources (Level: $levelDescription)."
        )
        
        // Under high pressure, clear transient logs or cached graphic contexts gracefully
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            DiagnosticsManager.clearDiagnostics()
            System.gc()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        DiagnosticsManager.log(
            category = DiagnosticCategory.SETTINGS_CHANGED,
            severity = DiagnosticSeverity.ERROR,
            message = "Critical low memory warning received. Performing deep resource reclamation."
        )
        DiagnosticsManager.clearDiagnostics()
        System.gc()
    }
}

