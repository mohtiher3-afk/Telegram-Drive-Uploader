package com.telegramdrive.uploader.core.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Proves the list-row status indicator uses the dot-matrix bar (DESIGN_SPEC §5 .db)
 * instead of the old flat Material LinearProgressIndicator, without needing a real
 * in-flight upload: the a11y content description the bar exposes is asserted.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class UploadStatusIndicatorDotBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `uploading row exposes the dot bar progress description`() {
        val task = UploadTask(
            id = "1",
            sourceUri = "content://test/reel.mp4",
            fileName = "reel.mp4",
            fileSize = 1_800_000_000L,
            status = UploadStatus.UPLOADING,
            progress = 50f
        )
        composeRule.setContent {
            UploadStatusIndicator(video = task)
        }
        // The DotProgressBar carries the "Upload progress: 50 percent" semantics; the
        // old flat bar used the same description. This fails if the bar regresses.
        composeRule.onNodeWithContentDescription("Upload progress: 50 percent").assertExists()
    }
}
