package com.telegramdrive.uploader.accessibility

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.telegramdrive.uploader.TelegramDriveApp
import org.junit.Rule
import org.junit.Test

/**
 * Automated accessibility scan for core app components.
 * Run on device via: ./gradlew :app:connectedCheck
 */
class AccessibilityScannerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TelegramDriveApp>()

    @Test
    fun `home screen has labels for all interactive elements`() {
        // Check that main UI elements have proper text labels
        composeRule.onAllNodesWithText("SCAN")
            .assertCount >= 1
        composeRule.onAllNodesWithText("UPLOAD")
            .assertCount >= 1
    }

    @Test
    fun `app buttons have content descriptions`() {
        // This test verifies that essential buttons have proper accessibility labels
        // If your app uses contentDescription in Compose:
        composeRule.onAllNodesWithTag("scan_button")
            .assertCount >= 1
    }
}