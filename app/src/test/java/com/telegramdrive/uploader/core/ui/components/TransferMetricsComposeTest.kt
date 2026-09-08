package com.telegramdrive.uploader.core.ui.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.telegramdrive.uploader.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TransferMetricsComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val ctx: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `renders the card and both metric columns`() {
        composeRule.setContent {
            TransferMetrics(speed = 5_242_880L, eta = 120L, progressFraction = 0.5f)
        }

        composeRule.onNodeWithTag("transfer_metrics_card").assertIsDisplayed()
        composeRule.onNodeWithTag("speed_metrics_col").assertIsDisplayed()
        composeRule.onNodeWithTag("eta_metrics_col").assertIsDisplayed()
    }

    @Test
    fun `speed and eta icons expose content descriptions for accessibility`() {
        composeRule.setContent {
            TransferMetrics(speed = 1_048_576L, eta = 60L, progressFraction = 0.25f)
        }

        val speedDesc = ctx.getString(R.string.upload_speed_desc)
        val etaDesc = ctx.getString(R.string.time_remaining_desc)
        assertTrue("speed/eta labels differ", speedDesc != etaDesc)
        composeRule.onNodeWithContentDescription(speedDesc).assertExists()
        composeRule.onNodeWithContentDescription(etaDesc).assertExists()
    }

    @Test
    fun `both labeled metric rows render their text`() {
        composeRule.setContent {
            TransferMetrics(speed = 2_097_152L, eta = 90L, progressFraction = 0.6f)
        }

        composeRule.onNode(hasText("Speed:", substring = true)).assertExists()
        composeRule.onNode(hasText("Remaining:", substring = true)).assertExists()
    }
}
