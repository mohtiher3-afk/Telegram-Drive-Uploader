# Sprint 34 — Baseline (2026-09-10, HEAD `9c965cb` + Sprint 31/32 fixes)

Missing referenced docs (never created, not blocking): `SPRINT_31_FINAL_REPORT.md`,
`SPRINT_33_UI_AUDIT.md`, `SPRINT_33_UI_IMPLEMENTATION.md` (Sprint 33 never ran;
no UI files changed since Stage-1 baseline — regression risk from UI work is nil).
Baseline sources used instead: `SPRINT_31_AUDIT.md`, `SPRINT_32_*` docs, source.

- Build: AGP/Kotlin via version catalog; `compileSdk 37`, `minSdk 24`, `targetSdk 36`;
  Java 21 locally. `buildDir` redirected to `%TEMP%\tdg-build`.
- Tests: 101 unit tests green (Stage-1 baseline); 2 androidTests
  (`TdLibRuntimeSmokeTest`, `SettingsDataStorePersistenceTest`).
- TDLib: v1.8.66 bindings + prebuilt `libtdjni.so` (+ssl/crypto) for
  `arm64-v8a` (58 MB) / `armeabi-v7a` (23 MB) / `x86_64` (36 MB). Fake JNI shim
  deleted (Sprint 31). Cloud smoke 4/4 green (API 33–36).
- DB: Room `AppDatabase` version **5**, `exportSchema = false`, single `uploads` table.
- WorkManager: unique work per upload id, exp backoff 30s, max 5 attempts,
  foreground `dataSync`, `reconcileInterruptedUploads()` on boot.
- Upload arch: picker → owned snapshot → `PreliminaryUploadFile` → `SendMessage` →
  `UpdateMessageSendSucceeded`-gated `Completed` (+`messageLink`); fail-closed policy.
- Release: `release { minifyEnabled, proguard-rules.pro }`; signing env-gated
  (`RELEASE_KEYSTORE_BASE64/PATH`, false locally → unsigned local release APK).
- Security-sensitive: BuildConfig API id/hash (build-time, not in git), DataStore
  session metadata, FileProvider authorities via `${applicationId}`; no secret
  logging found (sweep clean).
- Known blockers: (1) manual real-device E2E never run (no local ADB/AVD);
  (2) `openrouter/free` lottery ~60% green; (3) pre-existing `Repository=FAIL`
  (tracked `releases/*.apk`); (4) SENDING state absent (cosmetic);
  (5) **retry-after-send window can duplicate a Telegram message**
  (§6 fix target: persist provisional message id, await confirmation, never
  blind-resend).
