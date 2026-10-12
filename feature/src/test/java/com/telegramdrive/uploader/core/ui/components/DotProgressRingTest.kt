package com.telegramdrive.uploader.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class DotProgressRingTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders the centre percentage for a half-filled ring`() {
        composeRule.setContent {
            DotProgressRing(
                progress = 0.5f,
                contentDescriptionText = "50%"
            )
        }

        // Centre readout shows the percentage.
        composeRule.onNodeWithText("50%").assertIsDisplayed()
    }

    @Test
    fun `exposes a progress semantics description for accessibility`() {
        composeRule.setContent {
            DotProgressRing(
                progress = 0.75f,
                contentDescriptionText = "75%"
            )
        }

        composeRule.onNodeWithContentDescription("75%").assertExists()
    }

    @Test
    fun `clamps progress and renders the rounded centre value`() {
        composeRule.setContent {
            // Out-of-range input must not crash or render a nonsense percentage.
            DotProgressRing(progress = 1.6f)
        }

        composeRule.onNodeWithText("100%").assertExists()
    }
}
