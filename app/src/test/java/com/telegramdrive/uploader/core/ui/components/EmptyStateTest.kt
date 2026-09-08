package com.telegramdrive.uploader.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EmptyStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `renders title and supporting text`() {
        composeRule.setContent {
            EmptyState(
                icon = Icons.Default.Info,
                title = "No uploads yet",
                supportingText = "Pick a video to get started"
            )
        }

        composeRule.onNodeWithText("No uploads yet").assertIsDisplayed()
        composeRule.onNodeWithText("Pick a video to get started").assertIsDisplayed()
    }

    @Test
    fun `shows no action button when action is omitted`() {
        composeRule.setContent {
            EmptyState(
                icon = Icons.Default.Info,
                title = "Empty",
                supportingText = "Nothing here"
            )
        }

        composeRule.onNodeWithText("Add file").assertDoesNotExist()
    }

    @Test
    fun `action button is displayed and invokes its callback exactly once per click`() {
        var clicks = 0
        composeRule.setContent {
            EmptyState(
                icon = Icons.Default.Info,
                title = "Empty",
                supportingText = "Nothing here",
                actionText = "Add file",
                onActionClick = { clicks++ }
            )
        }

        composeRule.onNodeWithText("Add file").assertIsDisplayed()
        composeRule.onNodeWithText("Add file").performClick()
        assertEquals(1, clicks)
    }
}
