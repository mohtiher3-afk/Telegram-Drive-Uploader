package com.telegramdrive.uploader.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.DynamicColorStrategy
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
class GlassComponentsRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Composable
    private fun GlassCardPreview(emphasis: LiquidGlassEmphasis = LiquidGlassEmphasis.Operational) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
                .height(120.dp),
            emphasis = emphasis
        ) {
            Text(
                text = "Glass card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(AppSpacing.lg)
            )
        }
    }

    @Test
    fun glassCard_light() {
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = false,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand
            ) {
                GlassCardPreview()
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun glassCard_dark() {
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = true,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand
            ) {
                GlassCardPreview()
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun glassCard_emphasis_variants() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                androidx.compose.foundation.layout.Column {
                    LiquidGlassEmphasis.entries.forEach { emphasis ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.xs)
                                .height(64.dp),
                            emphasis = emphasis
                        ) {
                            Text(
                                text = emphasis.name,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(AppSpacing.md)
                            )
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun shimmerPlaceholder_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                        .height(80.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun shimmerPlaceholder_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                        .height(80.dp)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun glassProgressIndicator_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                GlassProgressIndicator(
                    progressFraction = 0.65f,
                    statusColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun glassProgressIndicator_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                GlassProgressIndicator(
                    progressFraction = 0.65f,
                    statusColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md)
                )
            }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
