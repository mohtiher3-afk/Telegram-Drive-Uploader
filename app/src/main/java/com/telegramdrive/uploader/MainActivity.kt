package com.telegramdrive.uploader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import dagger.hilt.android.AndroidEntryPoint
import com.telegramdrive.uploader.core.navigation.AppNavigation
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Trigger rebuild to ensure correct app is installed on the emulator
    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Prevent screenshots and screen recording of sensitive upload data.
        // Every shipped build sets BuildConfig.ALLOW_SCREENSHOTS=true, so the flag is
        // always applied there. Only the debug build type sets it false, to let a
        // developer capture the UI; that override lives in app/build.gradle.kts.
        if (BuildConfig.ALLOW_SCREENSHOTS) {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        enableEdgeToEdge()
        setContent {
            val themePreference by settingsDataStore.themePreference.collectAsStateWithLifecycle(initialValue = "System")
            val dynamicColorEnabled by settingsDataStore.dynamicColorEnabled.collectAsStateWithLifecycle(initialValue = true)
            val darkTheme = when (themePreference) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }
            TelegramDriveTheme(
                darkTheme = darkTheme,
                dynamicColorEnabled = dynamicColorEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DesignTokens.AppColors.background
                ) {
                    AppNavigation(settingsDataStore)
                }
            }
        }
    }
}
