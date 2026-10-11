package com.telegramdrive.uploader.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Proves the active-transfer card renders the dot-matrix ring + parts map from
 * the design spec (docs/maintenance/DESIGN_SPEC.md §5) without needing a real
 * in-flight upload: we drive completed/total directly.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class ActiveTransferSummaryTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `half-filled transfer shows the ring readout and parts summary`() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                // completed=2, total=4 -> 50%
                ActiveTransferSummary(
                    fileName = "demo_reel.mp4",
                    completedCount = 2,
                    pendingCount = 2,
                    total = 4
                )
            }
        }
        // Ring centre shows 50%.
        composeRule.onNodeWithText("50%").assertIsDisplayed()
        // Parts summary: 2 of the 36 parts that the ring tracks.
        composeRule.onNodeWithText("2 of 36 parts sent").assertExists()
        // And the pending count beneath it.
        composeRule.onNodeWithText("2 pending").assertExists()
    }
}
