package com.telegramdrive.uploader.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EmptyStateRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureEmptyStateScreenshot() {
        composeRule.setContent {
            EmptyState(
                icon = Icons.Outlined.CloudUpload,
                title = "No files yet",
                supportingText = "Uploaded files will appear here."
            )
        }
        composeRule.onRoot().captureRoboImage()
    }
}
