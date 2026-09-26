package com.telegramdrive.uploader.core.ui.components

import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.theme.TelegramDriveTheme
import com.telegramdrive.uploader.core.ui.theme.GlowColorPreset
import com.telegramdrive.uploader.core.ui.theme.DynamicColorStrategy
import org.junit.Test
import org.roborazzi.Roborazzi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTestTag
import androidx.compose.ui.test.assertExists
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telegramdrive.uploader.core.ui.components.LiquidGlassEmphasis
import com.telegramdrive.uploader.core.ui.components.LottieAnimations
import com.telegramdrive.uploader.feature.R

class GlassComponentsRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun glassCard_light() {
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = false,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand
            ) {
                Surface {
                    GlassCardPreview()
                }
            }
        }
        Roborazzi.takeScreenshot(composeRule, "glass_card_light")
    }

    @Test
    fun glassCard_dark() {
        composeRule.setContent {
            TelegramDriveTheme(
                darkTheme = true,
                dynamicColorStrategy = DynamicColorStrategy.StaticBrand
            ) {
                Surface {
                    GlassCardPreview()
                }
            }
        }
        Roborazzi.takeScreenshot(composeRule, "glass_card_dark")
    }

    @Test
    fun glassCard_emphasis_variants() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                EmphasisVariantsPreview()
            }
        }
        Roborazzi.takeScreenshot(composeRule, "glass_card_emphasis_variants")
    }

    @Test
    fun emptyState_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Default.VideoLibrary,
                    title = "No videos",
                    supportingText = "Add videos to upload",
                    animation = LottieAnimations.emptyUpload
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "empty_state_light")
    }

    @Test
    fun emptyState_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Default.VideoLibrary,
                    title = "No videos",
                    supportingText = "Add videos to upload",
                    animation = LottieAnimations.emptyUpload
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "empty_state_dark")
    }

    @Test
    fun errorState_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                ErrorState(
                    message = "Upload failed. Please retry.",
                    animation = LottieAnimations.uploadError
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "error_state_light")
    }

    @Test
    fun errorState_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                ErrorState(
                    message = "Upload failed. Please retry.",
                    animation = LottieAnimations.uploadError
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "error_state_dark")
    }

    @Test
    fun shimmerPlaceholder_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                ShimmerPlaceholderPreview()
            }
        }
        Roborazzi.takeScreenshot(composeRule, "shimmer_placeholder_light")
    }

    @Test
    fun shimmerPlaceholder_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                ShimmerPlaceholderPreview()
            }
        }
        Roborazzi.takeScreenshot(composeRule, "shimmer_placeholder_dark")
    }

    @Test
    fun glassProgressIndicator_light() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = false) {
                GlassProgressIndicator(
                    progressFraction = 0.65f,
                    statusColor = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(AppSpacing.md)
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "glass_progress_indicator_light")
    }

    @Test
    fun glassProgressIndicator_dark() {
        composeRule.setContent {
            TelegramDriveTheme(darkTheme = true) {
                GlassProgressIndicator(
                    progressFraction = 0.65f,
                    statusColor = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(AppSpacing.md)
                )
            }
        }
        Roborazzi.takeScreenshot(composeRule, "glass_progress_indicator_dark")
    }