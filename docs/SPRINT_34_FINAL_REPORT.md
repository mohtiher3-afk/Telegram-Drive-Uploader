# Sprint 34 — Final Report: Production Hardening & Real Device QA

## 1. Baseline
`SPRINT_34_BASELINE.md`. Missing refs noted (S31 final report, S33 docs never
existed — no UI drift since Stage-1). HEAD at start: `9c965cb`.

## 2. Bugs found
1. **Retry-after-send duplicates Telegram messages (CRITICAL, fixed).** Worker retry
   re-ran `PreliminaryUploadFile` + `SendMessage`; provisional id lived only in RAM.
   A retry in the sent-but-unconfirmed window delivered the video twice.
2. arm64 `libcrypto/ssl.so` absent (investigated → NOT a bug: arm64 `libtdjni.so`
   statically links crypto; no DT_NEEDED; 58 MB corroborates).
3. No `SENDING` status / raw message IDs (cosmetic; deferred with rationale).

## 3. Repairs
- `UploadEntity` + `UploadTask` gain nullable `provisionalMessageId`.
- Room v5→v6 `MIGRATION_5_6` (+ test), registered in `DatabaseModule`.
- `TelegramUploadEvent.MessageSent`; engine persists the id at send time.
- On retry with provisional id: buffered-confirm → Success; else 60 s await →
  Success; else non-retryable ambiguous FAILED (never blind-resend).
- `TelegramClient.takeBufferedSendSuccess/awaitSendConfirmation` with
  double-delivery guard; waiters cancelled on `closeClient()`.

## 4. Files changed
`UploadEntity.kt`, `AppDatabase.kt` (+migration), `DatabaseModule.kt`,
`UploadDao.kt`, `UploadRepository.kt`, `UploadRepositoryImpl.kt`,
`UploadTask.kt`, `TelegramClient.kt`, `TelegramClientImpl.kt`,
`TelegramUploadEngineImpl.kt`, test fakes (`UploadWorkerTest`,
`TelegramUploadEngineAuthGateTest`, `QueueScreenComposeTest`),
new `DatabaseMigrationTest.kt` + 3 engine idempotency tests.

## 5–10. Findings (memory/perf/worker/lifecycle/db/security)
Streaming throughout (1 MB chunks, owned snapshots, retriever/codec releases);
unique-work idempotency keys; exp backoff + 5-attempt bound; single `@Singleton`
client with lock-guarded create/close; destructive-migration fallback retained
behind explicit migration; zero hardcoded secrets; manifest minimal
(1 exported launcher activity); single notification channel; no full-RAM reads.

## 11–14. Device/E2E/build/test
Device E2E still manual-only (`SPRINT_32_E2E_CHECKLIST.md`). Builds: debug +
unsigned release green. Tests: **105/105**.

## 15. Remaining blockers
Manual real-device run (auth → upload → confirm). Nothing else blocks.
