# Sprint 32 — Repair Plan (from verified Sprint 31 findings)

Note: `docs/SPRINT_31_FINAL_REPORT.md` was never written (deferred pending a real
device test). This plan is built from `docs/SPRINT_31_AUDIT.md` re-verified
against current source (auth machine, send gating, cancel wiring, mock sweep —
all confirmed 2026-09-10).

## Issue classification

### CRITICAL — none remaining
- S31-K1 (fake JNI shim `tdjni_bridge.cpp`): **already repaired** — files deleted
  (`app/src/main/cpp/tdjni_bridge.cpp`, `CMakeLists.txt`), commit `bc7fd7e`.
  Verified: no `externalNativeBuild` in any `*.kts`; app loads prebuilt `.so`.

### HIGH — none in production paths
- Auth state machine complete, no bypass, no hardcoded credentials, no secret
  logging (`TelegramClientImpl.kt:407-432`, `sendTdlibParameters` 434-461).
- Upload gated on `UpdateMessageSendSucceeded` matched by `(chatId, oldMessageId)`
  (`TelegramClientImpl.kt:380-392`); fail-closed `UploadCompletionPolicy`.

### MEDIUM — both repaired in Sprint 31 fix round
1. **Stale restored destination** — repaired: `UploadViewModel` revalidates the
   saved ID against the first non-empty live list; on absence clears memory +
   `SettingsDataStore.clearSelectedDestination()` (new). Submit path still
   enforces `TelegramDestinationPolicy.isSelectable` + engine rejects id 0.
2. **Cancel never reached TDLib** — repaired: new `cancelActiveUploads()` on
   `TelegramClient` → `TelegramUploadEngine` → called from `UploadWorker`'s
   `isStopped` paths; sends `TdApi.CancelPreliminaryUploadFile` per pending fileId.
   Test fakes updated (`FakeEngine.cancelCalls`, `FakeTelegramClient` no-op).

### LOW — deferred with rationale (not repaired)
- No `SENDING` status: `UPLOADING` at 100% + provisional message ID already
  distinguishes "sent, awaiting confirmation". Adding a value without a distinct
  trigger would be cosmetic churn (violates "don't rewrite").
- `UploadEntity` stores `messageLink`, not raw Telegram message/file IDs: link is
  derivable evidence; schema migration for raw IDs is unjustified now.
- First-batch-only `GetChats`: mitigated by update stream + server search.
- No `TelegramClientImpl` unit tests: client is a thin callback shell over
  `Client.create`; policies/guards around it are tested (32 unit tests green).

## Repair order (dependency + impact)
1. ✅ Delete fake shim (done, `bc7fd7e`).
2. ✅ Destination revalidation (done, `bc7fd7e`).
3. ✅ Cancel wiring (done, `bc7fd7e`).
4. Phase 11 builds/tests (this sprint).
5. Manual device E2E per `SPRINT_32_E2E_CHECKLIST.md` (user-side; no emulator/ADB
   device available locally, no AVD configured).
