# PHASE 07 — Regression evidence (honest, current)

**Date:** 2026-09-19 · **TRUTH: no compile pass, no device pass claimed.**

## Verified facts (from source reads this session, not assertions)
- Device on-line: `BUFYHQZXR4BQK7WK device product:zircon_global model:23090RA98G` (arm64-v8a).
- REAL terminal enum member: `UploadStatus.COMPLETED`
  (`app/src/main/java/com/telegramdrive/uploader/domain/model/UploadStatus.kt`;
  enum = QUEUED, PREPARING, UPLOADING, PAUSED, RETRYING, COMPLETED, FAILED, CANCELLED — NO SUCCEEDED).
- `UploadWorker` writes `UploadStatus.COMPLETED` as its terminal DB status (line 179).
- Wrapseam: `TelegramUploadEngine` (domain.upload) + sealed `UploadEngineResult`
  { Progress, Success(uploadDurationMs, messageLink?), Error(message, isRetryable) }.
- Test seam pattern (no Hilt, direct object construction) = TdLibRuntimeSmokeTest /
  SettingsDataStorePersistenceTest; regression tests live under
  `app/src/androidTest/java/com/telegramdrive/uploader/regression/`.

## What the 3 files assert (fail-then-pass)
1. `UploadChainRegressionTest.kt` — chain must reach real `UploadStatus.COMPLETED`;
   fail leg = stalled engine (never terminal), pass leg = terminal engine.
2. `ChannelSearchSeparatorRegressionTest.kt` — separator-normalized search seam.
3. `Phase07RegressionTest.kt` — combined seam guard.

## HONEST GAP (not hidden)
- Compile of androidTest has NOT succeeded (Gradle daemon JVM crash ×3:
  `hs_err_pid8700.log`, `Native memory allocation (mmap) failed … paging file too small`).
- = no `assembleDebugAndroidTest`, no device instrumentation run, NO claimed PASS.
- Evidence of real device presence IS logged (adb), but execution requires: host
  memory headroom + recompile + instrument run. That run is the NEXT step on your go.

---

## 2026-09-19 — CORRECTION committed to the file set (no run, no fabrication)

**What the compiler gate caught (the honest fail):** the earlier `UploadChainRegressionTest`
referenced `UploadStatus.SUCCEEDED` + an invented `UploadRepositoryImpl(uploadDao: UploadDao)`
constructor. Verified reality: the enum's terminal member is **`UploadStatus.COMPLETED`**
(enum = QUEUED/PREPARING/UPLOADING/PAUSED/RETRYING/COMPLETED/FAILED/CANCELLED — no SUCCEEDED;
`UploadWorker.kt` line 179 writes `UploadStatus.COMPLETED`), and the worker builds its
repository via Hilt-assisted DI, not a public 2-arg constructor.

**Fix applied:** rewrote `UploadChainRegressionTest.kt` to assert the REAL seam:
- terminal status == `UploadStatus.COMPLETED` (the member `UploadWorker.kt:179` truly writes);
- stall leg: an engine that emits `Progress` and then `awaitCancellation()` (hangs) must
  NOT fold to `UploadStatus.COMPLETED` (assertNotEquals) — deterministic, TDLib-free,
  on-device-runnable, honest fail-then-pass driver.

**NOT claimed (never was):** no compile success, no assemble, no device run. The
`compileDebugAndroidTestKotlin` retry is gated on a host with working memory: the Gradle
daemon has crashed 3× (`hs_err_pid*.log` → native mmap / paging-file-too-small), not the code.

---

## 2026-09-19 — ATTEMPT LOOP #4 verdict (honest stop, not a fourth crash)

After the COMPLETED-seam correction, I retried the androidTest compile 4 times with
progressively smaller memory caps (daemon 23544 → 768m, then -Xmx512m, then dbg
single-worker, then -Xmx384m offline no-build-cache). Verdict: **this host cannot
start ANY Gradle JVM right now** — even the 384m launcher died at JVM startup with
`os::commit_memory(0x.., 257949696) failed … paging file is too small` (hs_err again).
This is a host paging-file limitation, reproduced 4/4, NOT an androidTest code error.

**Stopping deliberately.** Every additional retry only spawns another hs_err_pid*.log
and pressures an already-too-small pagefile. Code-level correction is DONE and on
disk (regressions now target REAL seams: UploadStatus.COMPLETED terminal,
TelegramUploadEngine + UploadEngineResult.Success, repository.updateStatus —
all verbatim-verified from source this session).

**Honest state (no fabrication):**
- Device BUFYHQZXR4BQK7WK: connected + authorized (confirmed via adb devices -l).
- Regression seams: corrected to REAL FQCNs, NOT compile-verified (host blocker),
  NOT device-run (blocked on compile), so NO fail-then-pass claimed on-device.
- No git add/commit/push performed. Awaiting explicit "نعم" for push — and a host
  moment where Gradle can actually start, for the on-device fail-then-pass run.
