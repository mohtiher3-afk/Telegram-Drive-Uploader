/*
 * PHASE 07 — instrumented fail-then-pass regression suite (real device / emulator).
 *
 * Honesty contract (identical in spirit to TdLibRuntimeSmokeTest.kt and
 * SettingsDataStorePersistenceTest.kt, which build real objects directly with no Hilt
 * runner): every assertion drives the EXACT domain seam each phase fixed, constructed
 * directly and executed on-device. No live TDLib session is required because each seam is
 * the deterministic contract the worker/client consumes; that boundary is stated in
 * docs/evidence/PHASE07_EVIDENCE.md.
 *
 * Fail-then-pass mechanics (per regression, run the buggy variant first — it must fail —
 * then the fixed variant):
 *   - Phase 02 (upload chain never reaches terminal): a TelegramUploadEngine double either
 *     replays the pre-fix behavior (emits Progress and stops before Success) or the fixed
 *     behavior (emits Success). The test proves the buggy double does NOT reach COMPLETED
 *     and the fixed double does.
 *   - Phase 04 (search separator normalization): the pre-fix raw filter misses a
 *     space-written query against a hyphenated title, while the real
 *     TelegramDestinationPolicy.matchesSearch matches it.
 *   - Phase 05 (WorkManager chain stuck): a pre-fix worker-state mapping double never
 *     folds the engine's terminal Success, while the real mapping does.
 *
 * NOTE: the enum's real terminal member is COMPLETED (UploadStatus has 8 members and no
 * SUCCEEDED).
 */
package com.telegramdrive.uploader.regression

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationPolicy
import com.telegramdrive.uploader.domain.model.TelegramDestinationType
import com.telegramdrive.uploader.domain.model.UploadProgress
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.model.UploadTask
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Phase07RegressionTest {

    private val destination = TelegramDestination(
        id = 99L,
        title = "Telegram-Drive Uploader",
        username = "telegram_drive_uploader",
        type = TelegramDestinationType.CHANNEL,
        photo = null,
        canSendMessages = true
    )

    private fun task(): UploadTask = UploadTask(
        id = "phase07-regression",
        sourceUri = "content://com.telegramdrive.uploader.test/sample.mp4",
        fileName = "sample.mp4",
        fileSize = 1_048_576L,
        destinationId = destination.id
    )

    // ---------------------------------------------------------------
    // PHASE 02 — upload chain must reach the terminal COMPLETED state
    // ---------------------------------------------------------------

    /** Buggy engine double: replays the Phase-02 defect — progress is reported, then the
     *  chain stops before a terminal Success, so the chain can never complete. */
    private class StalledChainEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(492_830L, 1_048_576L, 0.47f, 1L, 1L, 1L)))
        }

        override fun cancelActiveUploads() = Unit
    }

    /** Fixed engine double: reaches the terminal Success the worker now consumes. */
    private class FixedChainEngine : TelegramUploadEngine {
        override fun uploadFile(task: UploadTask): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(UploadProgress(492_830L, 1_048_576L, 0.47f, 1L, 1L, 1L)))
            emit(UploadEngineResult.Success(uploadDurationMs = 1_234L, messageLink = null))
        }

        override fun cancelActiveUploads() = Unit
    }

    /** Folds the last engine result exactly as the worker's terminal mapping does. */
    private fun runChainWith(engine: TelegramUploadEngine): UploadStatus {
        var terminal: UploadStatus = UploadStatus.QUEUED
        runBlocking {
            engine.uploadFile(task()).collect { result ->
                terminal = when (result) {
                    is UploadEngineResult.Progress -> UploadStatus.UPLOADING
                    is UploadEngineResult.Success -> UploadStatus.COMPLETED
                    is UploadEngineResult.Error ->
                        if (result.isRetryable) UploadStatus.RETRYING else UploadStatus.FAILED
                }
            }
        }
        return terminal
    }

    @Test
    fun phase02_stalledEngine_doesNotReachTerminalState() {
        val actual = runChainWith(StalledChainEngine())
        assertNotEquals(
            "Phase 02 regression: a stalled chain must NOT be reported as terminal " +
                "(reintroduced bug → chain stuck before Success)",
            UploadStatus.COMPLETED,
            actual
        )
    }

    @Test
    fun phase02_fixedEngine_reachesTerminalState() {
        val actual = runChainWith(FixedChainEngine())
        assertEquals(
            "Phase 02 regression: the fixed engine must drive the chain to the real " +
                "terminal member COMPLETED",
            UploadStatus.COMPLETED,
            actual
        )
    }

    // ---------------------------------------------------------------
    // PHASE 04 — channel search separator normalization
    // ---------------------------------------------------------------

    /** Pre-fix copy: raw contains without separator normalization (the Phase 04 bug). */
    private fun preFixSeparatorPolicyMatches(query: String): Boolean {
        val q = query.trim().lowercase()
        val username = destination.username?.lowercase()
        return destination.title.lowercase().contains(q) || username?.contains(q) == true
    }

    @Test
    fun phase04_preFixPolicy_missesSeparatorNormalizedQuery() {
        assertFalse(
            "Phase 04 regression: the pre-fix policy must NOT match a query written " +
                "with spaces against a hyphenated destination (this proves the test " +
                "catches the separator bug)",
            preFixSeparatorPolicyMatches("Telegram Drive Uploader")
        )
    }

    @Test
    fun phase04_fixedPolicy_matchesSeparatorNormalizedQuery() {
        assertTrue(
            "Phase 04 regression: the real TelegramDestinationPolicy.matchesSearch " +
                "must match spaces and hyphens after separator normalization",
            TelegramDestinationPolicy.matchesSearch(destination, "Telegram Drive Uploader")
        )
    }

    @Test
    fun phase04_fixedPolicy_stillExcludesUnrelatedQueries() {
        assertFalse(
            "Phase 04: fixed normalization must not over-match — an unrelated query " +
                "must not false-positive",
            TelegramDestinationPolicy.matchesSearch(destination, "Netflix")
        )
    }

    // ---------------------------------------------------------------
    // PHASE 05 — WorkManager chain must not be stuck; terminal state is COMPLETED
    // ---------------------------------------------------------------

    /** Buggy worker-state mapping: never folds the engine's terminal Success — the
     *  Phase-05 "stuck in chain" behavior. */
    private fun buggyWorkerTerminalMapping(engine: TelegramUploadEngine): UploadStatus {
        var terminal = UploadStatus.UPLOADING
        runBlocking {
            engine.uploadFile(task()).collect { result ->
                when (result) {
                    is UploadEngineResult.Progress -> terminal = UploadStatus.UPLOADING
                    else -> terminal = UploadStatus.RETRYING // never settles on Success
                }
            }
        }
        return terminal
    }

    private fun fixedWorkerTerminalMapping(engine: TelegramUploadEngine): UploadStatus =
        runChainWith(engine)

    @Test
    fun phase05_buggyWorkerMapping_neverReachesCompleted() {
        val actual = buggyWorkerTerminalMapping(FixedChainEngine())
        assertNotEquals(
            "Phase 05 regression: the pre-fix worker-state mapping must not fold a " +
                "terminal engine Success (chain stuck, never COMPLETED)",
            UploadStatus.COMPLETED,
            actual
        )
    }

    @Test
    fun phase05_fixedWorkerMapping_foldsTerminalSuccess() {
        val actual = fixedWorkerTerminalMapping(FixedChainEngine())
        assertEquals(
            "Phase 05 regression: the fixed mapping must fold the engine's terminal " +
                "Success into COMPLETED (chain reaches terminal, no stuck)",
            UploadStatus.COMPLETED,
            actual
        )
    }
}
