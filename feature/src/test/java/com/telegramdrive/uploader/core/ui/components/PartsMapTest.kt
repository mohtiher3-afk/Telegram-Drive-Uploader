package com.telegramdrive.uploader.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class PartsMapTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `shows the sent-of-total summary line`() {
        composeRule.setContent {
            PartsMap(totalParts = 36, sentParts = 18)
        }
        composeRule.onNodeWithText("18 of 36 parts sent").assertIsDisplayed()
    }

    @Test
    fun `exposes per-part semantics for done, active and pending`() {
        composeRule.setContent {
            PartsMap(totalParts = 36, sentParts = 18)
        }
        // Index 17 is the last sent part, 18 is the in-flight head, 19 pending.
        composeRule.onNodeWithContentDescription("Part 18 sent").assertExists()
        composeRule.onNodeWithContentDescription("Part 19 sending").assertExists()
        composeRule.onNodeWithContentDescription("Part 20 pending").assertExists()
    }

    @Test
    fun `failed part is announced and clickable to retry`() {
        var retried = false
        composeRule.setContent {
            PartsMap(
                totalParts = 36,
                sentParts = 18,
                failedPartIndex = 19,
                onFailedPartClick = { retried = true }
            )
        }
        composeRule.onNodeWithText("Part 19 failed. Tap to retry.").assertExists()
        composeRule.onNodeWithContentDescription("Part 19 failed").performClick()
        assertTrue("retry handler should fire on tapping the failed part", retried)
    }

    @Test
    fun `clamps sentParts to the total`() {
        composeRule.setContent {
            PartsMap(totalParts = 36, sentParts = 999)
        }
        composeRule.onNodeWithText("36 of 36 parts sent").assertExists()
    }
}
