package com.telegramdrive.uploader.data.telegram.client

import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.drinkless.tdlib.TdApi

class TelegramVideoMessageContentTest {
    @Test
    fun videoUsesInputMessageVideoAndConvertsDurationToSeconds() {
        val content = buildUploadMessageContent(task("video/mp4", 90_500L, 1920, 1080), 42)

        assertTrue(content is TdApi.InputMessageVideo)
        val video = (content as TdApi.InputMessageVideo).video
        assertEquals(90, video.duration)
        assertEquals(1920, video.width)
        assertEquals(1080, video.height)
        assertTrue(video.supportsStreaming)
        assertTrue(video.video is TdApi.InputFileId)
        assertEquals(42, (video.video as TdApi.InputFileId).id)
    }

    @Test
    fun nonVideoUsesInputMessageDocument() {
        val content = buildUploadMessageContent(task("application/pdf", 0L, 0, 0), 7)

        assertTrue(content is TdApi.InputMessageDocument)
        val document = (content as TdApi.InputMessageDocument).document
        assertTrue(document.document is TdApi.InputFileId)
        assertEquals(7, (document.document as TdApi.InputFileId).id)
    }

    private fun task(mimeType: String, duration: Long, width: Int, height: Int) = UploadTask(
        id = "test-upload",
        sourceUri = "content://test/source",
        fileName = "sample",
        fileSize = 1_000L,
        mimeType = mimeType,
        destinationId = -100123L,
        status = UploadStatus.QUEUED,
        progress = 0f,
        uploadedBytes = 0L,
        totalBytes = 1_000L,
        speed = 0L,
        averageSpeed = 0L,
        eta = 0L,
        createdAt = 1L,
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
