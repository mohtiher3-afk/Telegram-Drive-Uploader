# Sprint 31 — Phase 1: Real Telegram E2E Audit (static inspection, no code changed)

Date: 2026-09-10. Scope: `app/src/main` (~90 Kotlin files) + `app/src/main/cpp` + tests + scripts.
Method: full-path trace of auth → init → session → discovery → destination →
picker → WorkManager → upload → send → confirm → persist → history/UI, plus
keyword sweep (`mock|fake|demo|simulat|placeholder|hardcod`).

## A. Current architecture

- UI (Compose): `feature/{splash,home,onboarding,upload,queue,history,settings,telegram}` +
  `core/ui/{theme,navigation,components}`.
- Domain: `domain/{model,repository,upload}` — `UploadManager`, `TelegramUploadEngine`,
  `UploadCompletionPolicy` (fail-closed), `TelegramDestinationPolicy`, `SpeedCalculator`.
- Data: `TelegramClientImpl` (real `org.drinkless.tdlib.Client`), `TelegramUploadEngineImpl`,
  `UploadManagerImpl` (WorkManager), `UploadRepositoryImpl` (Room), `SettingsDataStore`
  (accounts, pins, selection, session metadata), `StreamingFileReader` (1 MB chunks).
- Worker: `feature/upload/worker/UploadWorker.kt` (foreground `dataSync`, exp backoff 30s,
  max 5 attempts, 1 Hz progress coalescing).
- Native: prebuilt `libtdjni.so` (+libcrypto/ssl) in `app/src/main/jniLibs` for
  `arm64-v8a` (58 MB), `armeabi-v7a` (23 MB), `x86_64` (36 MB); TDLib v1.8.66 bindings.
- `app/src/main/cpp/tdjni_bridge.cpp` + `CMakeLists.txt` exist but are **not wired**
  (no `externalNativeBuild` anywhere in `*.kts`).

## B. Telegram authentication flow — REAL, complete

`TelegramClientImpl.handleAuthorizationState` (lines 407–432) handles
`WaitTdlibParameters → sendTdlibParameters()` (real apiId/apiHash from BuildConfig,
per-account `tdlib-database-<key>` / `tdlib-files-<key>` dirs),
`WaitPhoneNumber / WaitCode / WaitPassword / WaitOtherDeviceConfirmation (QR link) /
Ready (GetMe + requestChats) / Closing / Closed`. Unknown states → `fail()` (no
bypass to READY). `sendPhoneNumber` requires leading `+`; blank code/password rejected.
`logout()` sends `LogOut`, closes client, clears session. No hardcoded credentials;
`isConfigured` rejects `placeholder_hash`. No secret values in logs (grep clean).

## C. TDLib initialization flow — REAL

`connect()` → `ensureNativeRuntime()` loads `libcrypto → libssl → libtdjni` by absolute
path with fallback, rethrows on failure (lines 621–658). Prebuilt `.so` verified:
`check-tdlib-artifacts.sh` (`TDLIB_CHECK_ABI=all`) PASS for all 3 ABIs + Java bindings.
Cloud smoke `android-device-smoke` 4/4 green (API 33–36, run 34505177826).

## D. Chat discovery flow — REAL with one limitation

`requestChats()` → `GetChats(ChatListMain, 100)` first batch, then `updateNewChat`
stream; search fans out to `SearchPublicChat / SearchPublicChats / SearchChatsOnServer`
(lines 220–240). `upsertChat/Supergroup`, permissions-aware `rebuildDestinations()`
(creator/admin-with-post-rights/restricted-with-send/private). Errors on lookup are
diagnostics-only, never fake auth failures. Limitation: initial list is first-batch
only (§K-4).

## E. Destination selection flow — REAL with staleness gap

`TelegramDestinationViewModel` (debounced search + pin sorting) →
`TelegramDestinationPolicy.isSelectable` (`id != 0 && canSendMessages && type != OTHER`) →
persisted id+title. Submit path re-checks policy + engine rejects `destinationId == 0`.
Gap: `UploadViewModel.init` rebuilds a destination from a saved ID with assumed
title/type (`"Saved Messages"`, USER, sendable) without revalidating the live list (§K-2).

## F. File upload flow — REAL

Picker → `takePersistableUriPermission` → readability + size check → snapshot
`content://` into app-owned storage (no full-RAM load; `StreamingFileReader` 1 MB
chunks) → `PreliminaryUploadFile(InputFileLocal, Video|Document, 32)` →
`UpdateFile` progress → `SendMessage(InputMessageVideo|InputMessageDocument)`.
Caption = filename; video needs valid width/height or falls back to document.

## G. Message sending flow — REAL, correctly gated

`SendMessage` returns a provisional message (explicitly NOT treated as proof) →
waits for `UpdateMessageSendSucceeded` matched on `(chatId, oldMessageId)` →
`Completed(messageLink)`; `UpdateMessageSendFailed` → classified retryable
(420/429/5xx). Out-of-order updates are buffered in `pendingMessageSuccesses/Failures`.
`t.me/c/<peer>/<msg>` link built for channels/supergroups.

## H. Persistence flow — REAL

Room `uploads` table: id/uri/name/size/mime/destinationId/status/progress/bytes/speed/
eta/created/started/completed/lastError/retryCount/thumbnail/dimensions/scheduledAt/
uploadDurationMs/messageLink. Explicit `UploadStatus` enum (no success boolean).
`UploadCompletionPolicy` is fail-closed: stream end without `Completed` → FAILED
(`UploadWorker` lines 230–246). Session: TDLib db dirs + DataStore metadata; restart
restores via `reconcileInterruptedUploads()`.

## I. Existing tests

32 unit tests (`app/src/test`): policies, guards, reconciliation, engine auth gate,
streaming reader, message-content builder, DAO, UI components. 2 androidTests:
`TdLibRuntimeSmokeTest`, `SettingsDataStorePersistenceTest`. FULL suite green.
Gap: no unit tests for `TelegramClientImpl` state machine/upload gating (§K-5).

## J. Existing CI validation

Multi-ABI CI green on pushes; device smoke 4/4 green; `tdlib-release-check`
(codex/openrouter, ~60% lottery) green 3/5 with end-to-end `#12`;
`verify-project.sh FULL` all PASS except pre-existing `Repository=FAIL`
(tracked `releases/*.apk`).

## K. Problems found

1. **CRITICAL — fake JNI shim in repo (currently dead code).**
   `app/src/main/cpp/tdjni_bridge.cpp` implements the real TDLib JNI entry points
   (`Java_org_drinkless_tdlib_Client_createNativeClient/nativeClientSend/
   nativeClientReceive/nativeClientExecute`, lines 343–532) backed by a fabricated
   event queue: `"remote_file_mock_id"` (lines 261, 283) and a demo QR session
   `"tg://login?token=AQAA_drive_uploader_demo_session"` with
   `EVENT_AUTH_STATE_WAIT_QR` (line 394). NOT compiled today (no `externalNativeBuild`
   in any `*.kts`; app loads prebuilt `.so`), so production is unaffected — but it is
   a landmine: wiring it would silently replace real Telegram with fake auth/upload,
   and building it would collide on `libtdjni.so` at packaging.
2. **MEDIUM — stale restored destination.** `UploadViewModel.init` (lines 79–98)
   fabricates title/type/permissions from a saved ID. Send path still uses the real
   ID, but UI can show a wrong name/type and skip revalidation.
3. **MEDIUM — cancel does not cancel TDLib-side upload.** `cancelUpload` only calls
   `cancelUniqueWork`; `TdApi.CancelPreliminaryUploadFile` (exists in bindings,
   `TdApi.java:44834`) is never invoked — in-flight bytes keep uploading.
4. **LOW — first-batch chat list.** Large accounts see incomplete destinations until
   updates/search fill in (§D). Search mitigates.
5. **LOW — status/model gaps.** `UploadStatus` has no `SENDING` (sprint asked for it);
   `UploadEntity` stores `messageLink` but not raw Telegram message/file IDs.
6. **LOW — no client state-machine unit tests** (§I).

## L. Severity

CRITICAL: K-1 (hygiene landmine; prod unaffected today). HIGH: none in prod paths.
MEDIUM: K-2, K-3. LOW: K-4, K-5, K-6.

## M. Recommended repair order

1. Delete (or quarantine out of `app/src/main`) `tdjni_bridge.cpp` + `CMakeLists.txt` —
   smallest change, kills the fake-behavior landmine. Re-run FULL.
2. Revalidate restored destination against live `_chatDestinations` on init; drop it
   (or mark unknown) when absent instead of fabricating metadata.
3. Call `CancelPreliminaryUploadFile(fileId)` on the cancel path (track fileId in
   worker input or repository).
4. Optional: add `SENDING` status + raw `telegramMessageId`; add unit tests for
   authorization-state mapping and upload gating with a faked `TelegramClient`
   interface (never touching real TDLib).
5. Device E2E on a real phone (manual): install debug APK → real login → destination →
   real upload → confirm message. Only then claim PASS for real-Telegram capabilities.
