package com.telegramdrive.uploader.regression

import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * PHASE 07 — the SAME fail-then-pass seam as the instrumented
 * `UploadChainRegressionTest`, but pure Kotlin/JVM so it executes on any host (and in CI)
 * without a device, emulator, TDLib session, or Hilt graph.
 *
 * The instrumented twin proves the seam on real hardware; this twin makes the regression
 * blocking on every build host. Both fold the engine result exactly as the worker's
 * terminal mapping does: Success -> COMPLETED, Error -> FAILED, Progress -> UPLOADING.
 */
class UploadChainSeamJvmTest {

    private val terminalTimeoutMs = 1_000L

    private fun task(): UploadTask = UploadTask(
        id = "phase07-chain-seam-jvm",
        sourceUri = "content://com.telegramdrive.uploader.test/sample.mp4",
        fileName = "sample.mp4",
        fileSize = 1_048_576L,
        destinationId = 99L
    )

    /** Folds the chain; a non-settling engine keeps its last (non-terminal) state. */
    private fun statusAfterChain(engine: TelegramUploadEngine): UploadStatus {
        var last: UploadEngineResult? = null
        runBlocking {
            withTimeoutOrNull(terminalTimeoutMs) {
                engine.uploadFile(task()).collect { result -> last = result }
            }
        }
        return when (last) {
            is UploadEngineResult.Success -> UploadStatus.COMPLETED
            is UploadEngineResult.Error -> UploadStatus.FAILED
            is UploadEngineResult.Progress -> UploadStatus.UPLOADING
            null -> UploadStatus.QUEUED
        }
    }

    /** Pre-fix behavior: progress is reported, then the chain stalls before Success. */
    private class StalledEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(512_000L, 1_048_576L, 0.47f, 1L, 1L, 1L)))
            awaitCancellation()
        }

        override fun cancelActiveUploads() = Unit
    }

    /** Fixed behavior: the chain reaches the terminal Success. */
    private class TerminalEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(262_144L, 1_048_576L, 0.25f, 1L, 1L, 1L)))
            emit(UploadEngineResult.Success(uploadDurationMs = 1_234L, messageLink = null))
        }

        override fun cancelActiveUploads() = Unit
    }

    @Test
    fun failLeg_stalledChainDoesNotReachCompleted() {
        val status = statusAfterChain(StalledEngine())
        assertNotEquals(
            "Fail leg: a chain that never emits Success must not read as COMPLETED.",
            UploadStatus.COMPLETED,
            status
        )
        assertEquals(
            "A stalled chain stays in the in-progress state.",
            UploadStatus.UPLOADING,
            status
        )
    }

    @Test
    fun passLeg_terminalChainReachesCompleted() {
        val status = statusAfterChain(TerminalEngine())
        assertEquals(
            "Pass leg: engine Success must fold to the real terminal member COMPLETED.",
            UploadStatus.COMPLETED,
            status
        )
    }

    @Test
    fun failThenPass_theFixIsWhatChangesTheOutcome() {
        val stalled = statusAfterChain(StalledEngine())
        val terminal = statusAfterChain(TerminalEngine())
        assertNotEquals("Fail-then-pass requires different outcomes.", stalled, terminal)
        assertEquals(UploadStatus.COMPLETED, terminal)
    }
}
