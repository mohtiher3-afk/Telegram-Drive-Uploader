package com.telegramdrive.uploader.feature.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w411dp-h891dp")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenThemeProofTest {

    @get:Rule
    val composeRule = createComposeRule()

    companion object {
        var darkSurface: Color = Color.Unspecified
        var darkOnSurface: Color = Color.Unspecified
        var darkPrimary: Color = Color.Unspecified

        var lightSurface: Color = Color.Unspecified
        var lightOnSurface: Color = Color.Unspecified
        var lightPrimary: Color = Color.Unspecified
    }

    @Test
    fun a_captureDarkThemeColors() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                darkSurface = MaterialTheme.colorScheme.surface
                darkOnSurface = MaterialTheme.colorScheme.onSurface
                darkPrimary = MaterialTheme.colorScheme.primary
            }
        }
    }

    @Test
    fun b_captureLightThemeColors() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                lightSurface = MaterialTheme.colorScheme.surface
                lightOnSurface = MaterialTheme.colorScheme.onSurface
                lightPrimary = MaterialTheme.colorScheme.primary
            }
        }
    }

    @Test
    fun c_themesDiffer() {
        // Ensure both were captured
        assertNotEquals("Dark surface should not be unspecified", Color.Unspecified, darkSurface)
        assertNotEquals("Light surface should not be unspecified", Color.Unspecified, lightSurface)

        assertNotEquals("Surface colors must differ between themes", lightSurface, darkSurface)
        assertNotEquals("OnSurface colors must differ between themes", lightOnSurface, darkOnSurface)
        assertNotEquals("Primary colors must differ between themes", lightPrimary, darkPrimary)
    }
}
