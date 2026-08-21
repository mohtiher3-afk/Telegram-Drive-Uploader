package com.telegramdrive.uploader.core.ai

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartFileAssistantTest {
    @Test
    fun preservesArabicFilenameAndAddsPortraitMetadata() {
        val suggestion = SmartFileAssistant.suggest(
            task(fileName = "تسجيل شاشة الهاتف.mp4", width = 480, height = 800)
        )

        assertTrue(suggestion.suggestedName.contains("تسجيل_شاشة_الهاتف"))
        assertTrue(suggestion.suggestedName.endsWith("_480x800.mp4"))
        assertTrue(suggestion.keywords.contains("تسجيل شاشة"))
        assertTrue(suggestion.keywords.contains("عمودي"))
    }

    @Test
    fun infersEnglishScreenRecordingKeywords() {
        val suggestion = SmartFileAssistant.suggest(
            task(fileName = "Screen recorder.mp4", width = 1920, height = 1080, duration = 61_000)
        )

        assertEquals("2025-08-21_Screen_recorder_1920x1080.mp4", suggestion.suggestedName)
        assertTrue(suggestion.keywords.contains("screen-recording"))
        assertTrue(suggestion.keywords.contains("أفقي"))
        assertTrue(suggestion.keywords.contains("وسائط"))
    }

    @Test
    fun fallsBackSafelyForMissingMetadataAndOddExtension() {
        val suggestion = SmartFileAssistant.suggest(
            task(fileName = "...", width = 0, height = 0, duration = 0)
        )

        assertTrue(suggestion.suggestedName.endsWith("_video.mp4"))
        assertEquals("task-1", suggestion.taskId)
        assertTrue(suggestion.keywords.isNotEmpty())
    }

    private fun task(
        fileName: String,
        width: Int = 0,
        height: Int = 0,
        duration: Long = 0L
    ) = UploadTask(
        id = "task-1",
        sourceUri = "content://example/video",
        fileName = fileName,
        fileSize = 8_780_000L,
        mimeType = "video/mp4",
        destinationId = 1L,
        status = UploadStatus.QUEUED,
        progress = 0f,
        uploadedBytes = 0L,
        totalBytes = 8_780_000L,
        speed = 0L,
        averageSpeed = 0L,
        eta = 0L,
        createdAt = 1_755_744_000_000L,
        startedAt = null,
        completedAt = null,
        lastError = null,
        retryCount = 0,
        thumbnailPath = null,
        duration = duration,
        width = width,
        height = height
    )
}
