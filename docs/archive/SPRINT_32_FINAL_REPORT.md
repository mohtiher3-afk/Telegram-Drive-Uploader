# Sprint 32 — Final Report: Real Telegram E2E Repair & Validation

## 1. Sprint 31 findings verified
All S31 audit claims re-checked against current source 2026-09-10: auth machine
intact, send gating on `(chatId, oldMessageId)`-matched `UpdateMessageSendSucceeded`
intact, zero fake-success/hardcoded-destination patterns in `app/src/main`
(post-fix sweep). Note: `SPRINT_31_FINAL_REPORT.md` never existed (was deferred
pending a device test); this report covers both sprints' evidence.

## 2. Repairs made
1. Deleted `app/src/main/cpp/tdjni_bridge.cpp` + `CMakeLists.txt` (fake JNI shim:
   `remote_file_mock_id`, demo QR session). Was dead code; removed as landmine.
2. Restored-destination revalidation in `UploadViewModel.init` + new
   `SettingsDataStore.clearSelectedDestination()`; stale IDs are dropped, never
   submitted with fabricated metadata.
3. `cancelActiveUploads()` on `TelegramClient`/`TelegramUploadEngine`, invoked
   from `UploadWorker` `isStopped` paths; sends `TdApi.CancelPreliminaryUploadFile`.
   Test fakes updated (`FakeEngine.cancelCalls`, `FakeTelegramClient` no-op).

## 3. Files changed
Deleted: `app/src/main/cpp/{tdjni_bridge.cpp,CMakeLists.txt}`.
Modified: `TelegramClient.kt`, `TelegramClientImpl.kt`, `TelegramUploadEngine.kt`,
`TelegramUploadEngineImpl.kt`, `UploadWorker.kt`, `SettingsDataStore.kt`,
`UploadViewModel.kt`, `UploadWorkerTest.kt`, `TelegramUploadEngineAuthGateTest.kt`.
Commits: `bc7fd7e` (fix), `93ebce9` (S31 audit doc).

## 4. Tests run
Targeted: `UploadWorkerTest`, `TelegramUploadEngineAuthGateTest`,
`UploadSubmissionGuardTest`, `TelegramDestinationPolicyTest` — green.
Full: `clean test assembleDebug` — BUILD SUCCESSFUL; **101 tests, 0 failures**,
matches Stage-1 baseline. `verify-project.sh FULL` green except pre-existing
`Repository=FAIL` (tracked `releases/*.apk`, unrelated).

## 5–10. Subsystem results (static + CI evidence, no device)
- Build: PASS. TDLib: PASS (prebuilt `.so` all 3 ABIs verified; cloud smoke 4/4
  API 33–36, run 34505177826; shim deleted).
- Authentication: PASS (static) — full state machine, no bypass, no secret logging.
- Chat discovery: PASS (static) — real APIs, permission-aware, first-batch noted.
- Destination: PASS (static) — policy gate + live revalidation added.
- File handling: PASS (static) — persistable perms, owned snapshot, 1 MB streaming.
- Worker: PASS (static) — foreground, bounded retry, fail-closed, TDLib cancel added.

## 11–13. Upload / Telegram confirmation / persistence
Code path is real and fail-closed end-to-end (PreliminaryUploadFile → UpdateFile →
SendMessage → UpdateMessageSendSucceeded → COMPLETED + messageLink; else FAILED).
**No live-device proof exists** — graded UNVERIFIED, never PASS, per sprint rules.

## 14. Persistence result
PASS (static): explicit statuses, messageLink, no success-boolean; reconcile on boot.

## 15. Remaining blockers
Single blocker: **manual real-device E2E** (`SPRINT_32_E2E_CHECKLIST.md`, 21 items:
install → login → session restore → discovery → destination → upload → progress →
confirm → message ID → COMPLETED → history → restart → verify in Telegram).
No local ADB device, no AVD configured — must run on the maintainer's phone with a
real account. Fresh debug APK: `%TEMP%\tdg-build\app\build\outputs\apk\debug\app-debug.apk`.

## Final matrix
AUTH: PASS (static) | TDLIB: PASS | CHAT DISCOVERY: PASS (static) |
DESTINATION: PASS (static) | FILE ACCESS: PASS (static) | WORKMANAGER: PASS (static) |
UPLOAD: UNVERIFIED | TELEGRAM CONFIRMATION: UNVERIFIED | ROOM: PASS (static) |
BUILD: PASS | TESTS: PASS (101/101)
