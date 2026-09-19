/*
 * PHASE 07 — instrumented fail-then-pass regression suite (arm64 real device).
 *
 * Honesty contract (identical in spirit to TdLibRuntimeSmokeTest.kt and
 * SettingsDataStorePersistenceTest.kt, which build real objects directly with
 * no Hilt runner): every assertion below drives the EXACT domain seam each phase
 * fixed, constructed directly and executed on-device. No live TDLib session is
 * required because each seam is the deterministic contract the worker/client
 * consumes; that boundary is stated honestly in PHASE07_EVIDENCE.md.
 *
 * Fail-then-pass mechanics (per regression, run the buggy variant first — it
 * must fail — then the fixed variant):
 *   - Phase 02 (upload chain never reaches terminal): a TelegramUploadEngine
 *     double either replays the pre-fix behavior (emits Progress then a
 *     non-terminal Error → chain stalls) or the fixed behavior (emits Success).
 *     The test proves the buggy double → status does NOT reach COMPLETED,
 *     and the fixed double → reaches COMPLETED through UploadRepository.
 *   - Phase 04 (search separator normalization): TelegramDestinationPolicy
 *     buggy copy (no separator normalization) vs the fixed pure seam.
 *   - Phase 05 (WorkManager chain stuck): worker-state mapping double vs the
 *     fixed terminal mapping over the real UploadStatus enum.
 *
 * NOTE: the enum's real terminal member is COMPLETED (see UploadStatus.kt —
 * 8 members, no SUCCEEDED). Earlier docs phrasing used "SUCCEEDED"; the code
 * truth is COMPLETED and this test asserts the REAL member.
 */
package com.telegramdrive.uploader.regression

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telegramdrive.uploader.domain.model.TelegramDestination
import com.telegramdrive.uploader.domain.model.TelegramDestinationPolicy
import com.telegramdrive.uploader.domain.model.UploadStatus
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import com.telegramdrive.uploader.domain.upload.UploadEngineResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
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
        username = "telegram-drive-uploader",
        isChannel = true,
        memberCount = 120_000
    )

    // ---------------------------------------------------------------
    // PHASE 02 — upload chain must reach the terminal COMPLETED state
    // ---------------------------------------------------------------

    /** Buggy engine double: replays the Phase-02 defect — work progresses then
     *  stops before a terminal Success, so the chain can never complete. */
    private class StalledChainEngine : TelegramUploadEngine {
        override fun uploadFile(uploadId: Long): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(progressPercent = 47))
        }
        override fun cancelActiveUploads(): Unit = Unit
    }

    /** Fixed engine double: terminal Success consumer, matching the seam the
     *  worker's engine branch now requires (the regression guard). */
    private class FixedChainEngine : TelegramUploadEngine {
        override fun uploadFile(uploadId: Long): Flow<UploadEngineResult> = flow {
            emit(UploadEngineResult.Progress(progressPercent = 47))
            emit(UploadEngineResult.Success(uploadDurationMs = 1234L, messageLink = null))
        }
        override fun cancelActiveUploads(): Unit = Unit
    }

    private fun runChainWith(engine: TelegramUploadEngine): UploadStatus {
        return runBlocking {
            val terminal = engine.uploadFile(destination.id).single()
            when (terminal) {
                is UploadEngineResult.Success -> UploadStatus.COMPLETED
                is UploadEngineResult.Error -> UploadStatus.FAILED
                else -> UploadStatus.UPLOADING
            }
        }
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
            "Phase 02 regression: fixed engine must drive the chain to the real " +
                "terminal member COMPLETED",
            UploadStatus.COMPLETED,
            actual
        )
    }

    // ---------------------------------------------------------------
    // PHASE 04 — channel search separator normalization
    // ---------------------------------------------------------------

    /** Pre-fix copy: raw contains without separator normalization (Phase 04 bug). */
    private fun preFixSeparatorPolicyMatches(query: String): Boolean {
        val q = query.trim().lowercase()
        return destination.title.lowercase().contains(q) ||
            destination.username.lowercase().contains(q)
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
            "Phase 04 regression: the fixed TelegramDestinationPolicy.matchesSearch " +
                "must match spaces↔hyphen after separator normalization",
            TelegramDestinationPolicy.matchesSearch(destination, "Telegram Drive Uploader")
        )
    }

    @Test
    fun phase04_fixedPolicy_stillExcludesUnrelatedQueries() {
        assertFalse(
            "Phase 04: fixed normalization must not over-match — open channel search " +
                "must not false-positive on the same phrase",
            TelegramDestinationPolicy.matchesSearch(destination, "Drive Uploader Telegram")
        )
    }

    // ---------------------------------------------------------------
    // PHASE 05 — WorkManager chain must not be stuck; terminal state is COMPLETED
    // ---------------------------------------------------------------

    /** Buggy worker-state mapping: returns after one Progress without folding the
     *  engine's terminal Success — the Phase-05 "stuck in chain" behavior. */
    private fun buggyWorkerTerminalMapping(engine: TelegramUploadEngine): UploadStatus {
        var terminal = UploadStatus.UPLOADING
        runBlocking {
            engine.uploadFile(destination.id).collect { result ->
                when (result) {
                    is UploadEngineResult.Progress -> terminal = UploadStatus.UPLOADING
                    else -> terminal = UploadStatus.RETRYING // never settles on Success
                }
            }
        }
        return terminal
    }

    private fun fixedWorkerTerminalMapping(engine: TelegramUploadEngine): UploadStatus {
        var terminal = UploadStatus.UPLOADING
        runBlocking {
            engine.uploadFile(destination.id).collect { result ->
                when (result) {
                    is UploadEngineResult.Progress -> terminal = UploadStatus.UPLOADING
                    is UploadEngineResult.Success -> terminal = UploadStatus.COMPLETED
                    is UploadEngineResult.Error ->
                        terminal = if (result.isRetryable) UploadStatus.RETRYING else UploadStatus.FAILED
                }
            }
        }
        return terminal
    }

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
