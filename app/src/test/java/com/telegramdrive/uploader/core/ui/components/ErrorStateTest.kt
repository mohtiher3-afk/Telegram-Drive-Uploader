package com.telegramdrive.uploader.core.ui.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ErrorStateTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val header: String =
        ApplicationProvider.getApplicationContext<Context>().getString(R.string.error_occurred)
    private val retryLabel: String =
        ApplicationProvider.getApplicationContext<Context>().getString(R.string.retry)

    @Test
    fun `shows the localized header and the supplied message`() {
        composeRule.setContent {
            ErrorState(message = "Network unreachable")
        }

        composeRule.onNodeWithText(header).assertIsDisplayed()
        composeRule.onNodeWithText("Network unreachable").assertIsDisplayed()
    }

    @Test
    fun `hides the retry action when no callback is provided`() {
        composeRule.setContent {
            ErrorState(message = "Bad config")
        }

        composeRule.onNodeWithText(retryLabel).assertDoesNotExist()
    }

    @Test
    fun `retry action is displayed and invokes its callback once per click`() {
        var retries = 0
        composeRule.setContent {
            ErrorState(message = "Transient failure", onRetryClick = { retries++ })
        }

        composeRule.onNodeWithText(retryLabel).assertIsDisplayed()
        composeRule.onNodeWithText(retryLabel).performClick()
        assertEquals(1, retries)
    }
}
