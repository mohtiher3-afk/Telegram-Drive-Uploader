package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class DotProgressBarRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun uploadProgressBar_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                DotProgressBar(
                    progress = 0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun uploadProgressBar_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                DotProgressBar(
                    progress = 0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
