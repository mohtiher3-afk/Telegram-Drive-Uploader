# AGENTS.md — Permanent Rules

- DEFINITION OF DONE: a task is only "done" when there is a working, runtime-verified change. Producing a Markdown report, audit, or "inventory" document does NOT count as done unless a document was explicitly the requested deliverable.
- EVIDENCE OVER CLAIMS: never write "verified" or "confirmed" without attaching the actual artifact (log output, test result, CI run link, screenshot). No artifact = write "not yet verified".
- ONE VERIFIED CHANGE AT A TIME: finish, test, and merge one fix before starting the next. Never accumulate multiple unverified phases before running an end-to-end check.
- REPRODUCE BEFORE YOU FIX: for any bug, first reproduce the failure with a log/screenshot/failing test. Only then write the fix.
- TEST THE FIX, NOT JUST THE BUILD: "CI is green" is not equivalent to "the feature works". Anything touching TDLib, WorkManager, upload state, or authentication requires a runtime check on a device/emulator before being marked resolved.
- NO SILENT SCOPE CREEP: do not touch files or architecture outside the explicit scope of the current phase file.
- SMALLEST POSSIBLE DIFF: prefer the minimal change that fixes the confirmed root cause over a broad refactor.
- MANDATORY REGRESSION TEST PER FIX: every confirmed bug fix ships with a test that fails on the old code and passes on the new code.
- CI IS A GATE, NOT A FORMALITY: nothing is merged to main with a red or skipped CI check.
- NO FAKE OR SIMULATED SUCCESS PATHS: never let a UI or log report "success" or "uploaded" based on local/staged state alone. Success must be gated on a confirmed signal from the real system.
- SECRETS AND ARTIFACTS DISCIPLINE: never commit keystores, API keys, .env values, session data, or built APK/AAB files. This covers build products of THIS project only; the third-party prebuilt native libraries under `data/src/main/jniLibs/` are an accepted exception documented in docs/architecture/adr-001-tdlib-binaries.md.
- COVERAGE IS MEASURED OR IT IS A GAP: report a coverage number only for a module that produced it in the same run. Kover is applied to `:feature` only, which measures **40.1% line** (2219/5536) and 27.0% branch coverage; `:data` carries no coverage plugin and is UNMEASURED, not zero and not passing. Run `:feature:koverXmlReport :feature:koverHtmlReport --rerun-tasks` for a newly computed figure; without `--rerun-tasks` Gradle reports `FROM-CACHE` and reuses a stale artifact. Never disable instrumentation to make a coverage task go green, that produces a fake pass and hides the module holding TDLib, the upload engine and the Room DAO.
- STALE RESULTS ARE NOT EVIDENCE: a cached or UP-TO-DATE task proves nothing. Gradle reads previous test outcomes back through `SerializableTestResult` before it runs anything, and a corrupt cache there aborts the task with `Illegal ArgumentException: Illegal Capacity: <negative>` before a single assertion executes, which is indistinguishable from a real failure. `scripts/verify-project.ps1` therefore clears every module's `build/test-results` before running, so each run reports outcomes it actually produced. Do not quote a test or coverage number without a matching artifact written by that run.
- HONEST LIMITATION REPORTING: if something cannot be fully verified (missing device, credentials, environment), say so explicitly. Never substitute documentation for real verification.
- STOP-AND-ASK TRIGGERS: stop and ask the user before (a) starting a new architecture-wide refactor, (b) publishing a signed release, (c) any action that could overwrite another engineer's in-progress work, (d) reducing test coverage to unblock a build.

## Agent working rules

- One small task per commit. Work on a branch, never on main. Never push without my approval.
- Before finishing any task, run `./scripts/verify-project.sh FULL` and report the real result. Never say "done" if it fails or was not run.
- Do not edit or replace libtdjni.so files, TdApi.java, or the R8 keep rules for org.drinkless.tdlib.** unless I explicitly ask.
- Never write api_id, api_hash, keystore files or passwords into any file. Use environment variables / Gradle properties only.
- App theme is dark-only. All user-facing text must support Arabic (RTL): use start/end instead of left/right, and no hardcoded strings.
- Before choosing or upgrading any library/plugin version, check the official source (developer.android.com). If the `android` CLI is installed, use `android docs search` / `android docs fetch` and `android studio version-lookup`; otherwise fetch the official release-notes page. Do not guess versions from memory.
- Upgrade one component at a time (AGP, then Kotlin, then Compose BOM, then others), run verify after each, and revert that step if it fails.
- If the same error repeats twice, stop, summarize what you tried, and ask me instead of looping.