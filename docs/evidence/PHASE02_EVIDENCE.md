# Phase 02 — PROVE REAL UPLOAD DELIVERY — EVIDENCE

STATUS: **DONE** (verified 2026-09-18, ~13:09 local)
Target: Redmi Note 13 Pro+ 5G, arm64-v8a, Android 16 / SDK 36, adb `BUFYHQZXR4BQK7WK`
APK: 1.0.24/24, package `com.aistudio.telegramdrive.prmuq`, `assembleDebug` installed via `adb install -r`

Raw evidence: `docs/evidence/phase02-upload-chain.logcat.txt` (3,260 lines, captured from device Logcat, pid 5166).

## Acceptance criterion 1 — Full Logcat, enqueue → confirmed TDLib message ID
Attached artifact: `phase02-upload-chain.logcat.txt`. Key markers (truncated):

- Process start + own init: `12:56:09 Start proc 5166:com.aistudio.telegramdrive.prmuq`
- `12:56:10 UploaderDiagnostics: [APP_START] Telegram Drive Uploader has successfully initialized`
- Upload sessions (data-plane conns to DcId 4): repeatedly `Session:4:upload#0..#3 ... Send query to connection [tl:0xde7b673d]`, `Flush write: +65820B`
- Progress reflected in queue UI (23% → 62% → 88% → done)
- **Confirmed delivery** `13:09:15`:
  ```
  Receive result for SendMediaQuery for -8223194439431686967: updates {
    updateMessageID { id = 474  random_id = -8223194439431686967 }
    updateReadChannelInbox { channel_id = 3767628510  max_id = 474 }
    updateNewChannelMessage {
      message {
        id = 474  out = true  post = true
        peer_id  = peerChannel { channel_id = 3767628510 }
        message  = "2026-09-18_VID_20260703_212859_634_480x1040.mp4"
        media    = messageMediaDocument { video = true  id = 5866456139813626410 }
      }
    }
  }
  ```
- Queued-work outcome (UI dump + queue tab): 3 COMPLETED / 0 failed / 0 pending, 181.19 MB. Also tracer stages shown in app: ENQUEUED → COMPRESSING → CONNECTING → UPLOADING → VERIFYING → COMPLETED.

## Acceptance criterion 2 — Visual confirmation from a separate device/account
User confirmed directly (2026-09-18): the uploaded file **`2026-09-18_VID_20260703_212859_634_480x1040.mp4` appeared in the target Telegram channel** (channel_id `3767628510`), viewed from the second device/account.

## Acceptance criterion 3 — Fixes required this phase (root cause + diff)
Three fixes were required; each was **re-run from the start** per the phase rules:

| # | Root cause | Fix | Commit / note |
|---|-----------|-----|---------------|
| 1 | `app/build.gradle.kts:78` hardcoded `storeFile = file("${rootDir}/debug.keystore")`; no such file existed (repo root has no debug keystore; `*.keystore` gitignored) → `validateSigningDebug` FAILED | Fallback to `~/.android/debug.keystore` when repo-root keystore absent (2-line change) | `1f10b4c` |
| 2 | sentry-android 8.51 auto-init ContentProvider runs **before** `Application.onCreate`, with no `io.sentry.dsn` in the manifest → device crash at boot `IllegalArgumentException: DSN is required` | Added `<meta-data android:name="io.sentry.auto-init" android:value="false" />` in `AndroidManifest.xml` so the app's guarded manual init owns Sentry | `7fd01bd` |
| 3 | No `.env` → `TelegramClientImpl` credential guard returns `Authentication Error` (app cannot even attempt login) | User supplied real `TELEGRAM_API_ID`/`TELEGRAM_API_HASH`; written to gitignored `.env`; **removed the empty `SENTRY_DSN=` line** (its empty value would have generated invalid `BuildConfig.java` `String SENTRY_DSN = ;` → javac `illegal start of expression`) | `.env` (untracked) + rebuild; verified injected into generated `BuildConfig` |

## Reprover chain used (phase rule: re-run entire test after each fix)
1. `:app:assembleDebug` BUILD SUCCESSFUL
2. `adb install -r` → Success
3. `am start -W` → launch; later: `uiautomator dump` UI dumps + `logcat -d --pid` at every stage
4. Login (real account MOH, OTP entered by user on device — shell input injection is blocked by the device's `INJECT_EVENTS` security)
5. Files picked from Telegram, queue ran, SendMedia delivered, user visually confirmed in Telegram client.

## Honors
Not modified in this phase: UI, theme, unrelated code (per DO NOT list). No unit-test substitutes were used.