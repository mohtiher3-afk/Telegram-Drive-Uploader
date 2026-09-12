package com.telegramdrive.uploader

import android.app.Application
import android.content.ComponentCallbacks2
import android.util.Log
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticsManager
import com.telegramdrive.uploader.core.diagnostics.DiagnosticCategory
import com.telegramdrive.uploader.core.diagnostics.DiagnosticSeverity
import dagger.hilt.android.HiltAndroidApp
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject

@HiltAndroidApp
class TelegramDriveApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize crash reporting (Sentry) only when a real DSN is configured.
        // Prevents Sentry from attempting network calls and leaking a placeholder DSN
        // when TELEGRAM_API_ID/HASH/SENTRY_DSN secrets are absent (e.g. in smoke builds).
        val sentryDsn = BuildConfig.SENTRY_DSN
        if (sentryDsn.isNotBlank() && sentryDsn != "YOUR_SENTRY_DSN_HERE") {
            SentryAndroid.init(this) { options ->
                options.dsn = sentryDsn
                options.tracesSampleRate = 0.25
            }
            DiagnosticsManager.log(
                category = DiagnosticCategory.CRASH_REPORTING,
                severity = DiagnosticSeverity.INFO,
                message = "Sentry crash reporting initialized in the Telegram Drive Uploader application."
            )
        } else {
            DiagnosticsManager.log(
                category = DiagnosticCategory.CRASH_REPORTING,
                severity = DiagnosticSeverity.WARN,
                message = "Sentry DSN not configured; crash reporting won't be enabled. Add SENTRY_DSN to .env to activate."
            )
        }

        DiagnosticsManager.log(
            category = DiagnosticCategory.APP_START,
            severity = DiagnosticSeverity.INFO,
            message = "Telegram Drive Uploader has successfully initialized in production environment."
        )
    }

    @Suppress("DEPRECATION")
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
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        DiagnosticsManager.log(
            category = DiagnosticCategory.SETTINGS_CHANGED,
            severity = DiagnosticSeverity.ERROR,
            message = "Critical low memory warning received. Releasing transient diagnostics cache."
        )
        DiagnosticsManager.clearDiagnostics()
    }
}

