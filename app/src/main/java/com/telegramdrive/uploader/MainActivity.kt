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
import com.telegramdrive.uploader.core.ui.theme.GlowColorPreset
import com.telegramdrive.uploader.core.ui.theme.DynamicColorStrategy
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
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContent {
            val themePreference by settingsDataStore.themePreference.collectAsStateWithLifecycle(initialValue = "System")
            val glowColorPreference by settingsDataStore.glowColorPreference.collectAsStateWithLifecycle(initialValue = "Seafoam")
            val customGlowHex by settingsDataStore.customGlowHex.collectAsStateWithLifecycle(initialValue = "69D6B5")
            val dynamicColorStrategy by settingsDataStore.dynamicColorStrategy.collectAsStateWithLifecycle(initialValue = "StaticBrand")
            val darkTheme = when (themePreference) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }
            val strategy = when (dynamicColorStrategy) {
                "Full" -> DynamicColorStrategy.Full
                "BrandAccented" -> DynamicColorStrategy.BrandAccented
                else -> DynamicColorStrategy.StaticBrand
            }
            TelegramDriveTheme(
                darkTheme = darkTheme,
                dynamicColorStrategy = strategy,
                glowColorPreset = GlowColorPreset.fromStorage(glowColorPreference),
                customGlowHex = customGlowHex
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
