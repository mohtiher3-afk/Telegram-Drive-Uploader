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
---

## 2026-09-20 — RESOLVED: the androidTest compile now succeeds (root cause was code, host was transient)

The earlier records above correctly reported a host paging-file limitation (the disk had
~0.2 GB free, so Gradle JVMs died during `mmap`). That was a **transient host condition**,
not a permanent property of the repository — with free disk restored the same host starts
Gradle normally (this run: compile finished in **8 seconds**).

Once the compile actually ran, it **immediately exposed real code defects in the regression
harness**. The previous note "NOT an androidTest code error" was therefore incorrect: it
could not be established while no compile could complete.

### Defects found by the compiler (verbatim)

| File | Defect |
|---|---|
| `ChannelSearchSeparatorRegressionTest.kt` | `TelegramDestination` constructed with `isChannel`, `date`, `memberCount`, `hasActiveUsernames`, `isVerified`, `isPremiumUser`, `isScam`, `isFake` — none of these parameters exist; `type`, `photo`, `canSendMessages` were missing |
| `Phase07RegressionTest.kt` | same destination defect; `uploadFile(uploadId: Long)` vs the real `uploadFile(task: UploadTask)`; `UploadEngineResult.Progress(progressPercent = 47)` vs the real `Progress(UploadProgress)` |
| `UploadChainRegressionTest.kt` | `uploadFile(taskId = 1L)`; `Progress(0.47f)`; the stall leg used `.single()`, which would have hung forever instead of failing deterministically; nested classes called an outer instance member |

### Fix applied

All three files were rewritten against the REAL APIs, read from source in the same session:

- `TelegramDestination(id, title, username: String?, type: TelegramDestinationType, photo: String?, canSendMessages: Boolean)`
- `TelegramUploadEngine.uploadFile(task: UploadTask): Flow<UploadEngineResult>`
- `UploadEngineResult.Progress(UploadProgress)` / `.Success(uploadDurationMs, messageLink)` / `.Error(message, isRetryable)`
- `UploadStatus` terminal member `COMPLETED`

The stall leg now collects the flow under a bounded `withTimeoutOrNull`, so a never-settling
chain yields its last non-terminal state deterministically instead of hanging the run.

### Evidence produced on this host (commands + results)

1. `:app:compileDebugAndroidTestKotlin` → **BUILD SUCCESSFUL in 8s** (previously failing).
2. `:app:assembleDebugAndroidTest` → **BUILD SUCCESSFUL**, artifact
   `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` (1 MB).
3. New pure-JVM twin `app/src/test/.../regression/UploadChainSeamJvmTest.kt`, executed:
   `:app:testDebugUnitTest --tests com.telegramdrive.uploader.regression.UploadChainSeamJvmTest`
   → `tests=3 failures=0 errors=0 skipped=0` in 2.2 s, cases:
   - `failLeg_stalledChainDoesNotReachCompleted` (1.013 s)
   - `passLeg_terminalChainReachesCompleted` (0.001 s)
   - `failThenPass_theFixIsWhatChangesTheOutcome` (1.176 s)

### Still NOT claimed

- **No on-device run.** No device is attached to this host at the time of writing
  (`adb devices` is empty), so the instrumented fail-then-pass result on real hardware
  remains unverified. The emulator lane in `.github/workflows/regression-gate.yml`
  (Gate 3) and a re-attached device are the paths to that evidence.
- No signing, no release, no push of release artifacts.

