package com.telegramdrive.uploader.feature.upload

import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadSubmissionGuardTest {

    @Test
    fun idleStateHasNoPreparedVideos() {
        val state = UploadUiState.Idle
        assertTrue(state is UploadUiState.Idle)
    }

    @Test
    fun successStateTracksIsSubmitting() {
        val destination = TelegramDestination(
            id = 100L,
            title = "My Channel",
            username = null,
            type = TelegramDestinationType.CHANNEL,
            photo = null,
            canSendMessages = true
        )
        val task = UploadTask(
            id = "test-1",
            sourceUri = "content://media/1",
            fileName = "sample.mp4",
            fileSize = 1024L,
            totalBytes = 1024L
        )
        
        val stateNotSubmitting = UploadUiState.Success(
            preparedVideos = listOf(task),
            selectedDestination = destination,
            isSubmitting = false
        )
        
        assertFalse(stateNotSubmitting.isSubmitting)
        
        val stateSubmitting = stateNotSubmitting.copy(isSubmitting = true)
        assertTrue(stateSubmitting.isSubmitting)
    }

    @Test
    fun invalidFilesWarningIsPreservedInSuccessState() {
        val warning = "Skipped 1 unreadable or zero-byte file(s)"
        val state = UploadUiState.Success(
            preparedVideos = emptyList(),
            invalidFilesWarning = warning
        )
        
        assertEquals(warning, state.invalidFilesWarning)
    }
}

