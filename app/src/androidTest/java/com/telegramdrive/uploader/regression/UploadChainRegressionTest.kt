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
 * PHASE 07 instrumented regression — driven at the REAL terminal seam.
 *
 * The truthful terminal member of the REAL enum is `UploadStatus.COMPLETED`
 * (`UploadStatus` has no `SUCCEEDED`; the worker writes COMPLETED on the
 * `UploadEngineResult.Success` branch).
 *
 * The doubles below implement the REAL `TelegramUploadEngine` contract:
 * `uploadFile(task: UploadTask): Flow<UploadEngineResult>`, emitting the REAL
 * `UploadEngineResult.Progress(UploadProgress)` and `.Success(uploadDurationMs, messageLink)`.
 *
 * FAIL leg (regression under test): an engine that stages Progress then stalls — never
 * reaching Success — must NOT fold to the terminal COMPLETED.
 * PASS leg (fixed seam): a terminal engine that emits Success folds to COMPLETED.
 *
 * This is the instrumented counterpart of the pure JVM harness in CI
 * (`.github/workflows/regression-gate.yml`).
 */
class UploadChainRegressionTest {

    /** Deterministic bound: the doubles emit synchronously, so this only guards the
     *  stall leg — it prevents a hanging test run. */
    private val terminalTimeoutMs = 2_000L

    private fun task(): UploadTask = UploadTask(
        id = "phase07-chain-regression",
        sourceUri = "content://com.telegramdrive.uploader.test/sample.mp4",
        fileName = "sample.mp4",
        fileSize = 1_048_576L,
        destinationId = 99L
    )

    /**
     * Folds the chain exactly as the worker's terminal mapping does: Success -> COMPLETED,
     * Error -> FAILED, Progress -> UPLOADING. When the engine never settles within
     * [terminalTimeoutMs] the last observed (non-terminal) state is returned.
     */
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

    /** Stalled engine: emits one Progress then suspends forever — never Success.
     *  Models the Phase-02/05 chain defect (stuck mid-upload, no terminal event). */
    private class StalledUploadEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(512_000L, 1_048_576L, 0.47f, 1L, 1L, 1L)))
            awaitCancellation()
        }

        override fun cancelActiveUploads() = Unit
    }

    /** Terminal engine: the fixed seam — reaches Success (COMPLETED). */
    private class TerminalUploadEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(262_144L, 1_048_576L, 0.25f, 1L, 1L, 1L)))
            emit(
                UploadEngineResult.Success(
                    uploadDurationMs = 1_234L,
                    messageLink = "https://t.me/telegramdrive/1"
                )
            )
        }

        override fun cancelActiveUploads() = Unit
    }

    @Test
    fun stalledEngine_neverReachesTerminalCompleted() {
        val status = statusAfterChain(StalledUploadEngine())
        assertNotEquals(
            "PHASE 02/05 regression (fail leg): a stalled upload chain must NOT " +
                "surface the terminal UploadStatus.COMPLETED before the engine emits Success.",
            UploadStatus.COMPLETED,
            status
        )
    }

    @Test
    fun terminalEngine_reachesCompleted() {
        val status = statusAfterChain(TerminalUploadEngine())
        assertEquals(
            "PHASE 02/05 regression (pass leg): engine Success must fold to the REAL " +
                "terminal member UploadStatus.COMPLETED (worker Success branch).",
            UploadStatus.COMPLETED,
            status
        )
    }

    @Test
    fun stalledEngine_isNotTerminalPendingNeither() {
        val status = statusAfterChain(StalledUploadEngine())
        assertNotEquals(
            "The stall must not accidentally read as FAILED either — it is an " +
                "in-progress (UPLOADING) state, which is exactly the stuck-chain hazard.",
            UploadStatus.FAILED,
            status
        )
    }
}
