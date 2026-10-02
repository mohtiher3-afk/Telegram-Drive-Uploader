# Upload State Testing

## Existing Automated Coverage Boundary

The repository contains focused unit tests for upload completion policy, WorkManager policy, upload timing/telemetry, and related upload behavior. The reviewed tests do not establish a complete transition algebra or real Telegram/WorkManager device lifecycle. No fake Telegram behavior was added to production code.

## Required Transition Matrix

| Scenario | Expected | Actual evidence | Status |
|---|---|---|---|
| Task creation | New task is persisted and unique work is enqueued. | `UploadViewModel` inserts each task then calls `UploadManager.enqueueUpload`. | PASS — repository verified |
| `QUEUED → PREPARING` | Worker loads task and enters preflight. | Worker writes `PREPARING` before engine invocation. | PASS — repository verified |
| `PREPARING → UPLOADING` | Real progress causes uploading state. | DAO progress update writes counters and `UPLOADING`. | PASS — repository verified; race boundary documented |
| `UPLOADING → COMPLETED` | Telegram-confirmed terminal event is required. | Engine emits success only on `TelegramUploadEvent.Completed`; Worker then writes completed. | PASS — repository verified |
| Active → `FAILED` | Terminal/unconfirmed/error path is persisted as failure. | Worker writes failure for non-retryable or unconfirmed outcomes. | PASS — repository verified |
| `FAILED → RETRYING` | Manual retry is bounded and re-enqueued. | ViewModel writes retrying; Worker caps attempts at five. | PASS — repository verified |
| Active → `PAUSED` | Pause updates state and cancels unique work. | ViewModel writes paused then calls `cancelUniqueWork`. | PASS — repository verified; race unverified |
| `PAUSED → QUEUED` | Resume re-enqueues task. | ViewModel writes queued and manager uses replacement policy. | PASS — repository verified |
| Active → `CANCELLED` | Cancellation persists and cancels work. | ViewModel writes cancelled then cancels unique work. | PASS — repository verified; race unverified |
| Progress bounds | No negative or >100 progress. | Engine clamps bytes and computes percentage in 0..100; UI bar clamps separately to 0..1. | CONDITIONAL — representation mismatch documented |
| Duplicate work | Same task should not have ordinary duplicate chains. | Normal enqueue uses unique work with KEEP; retry/resume use REPLACE. | CONDITIONAL — runtime concurrency unverified |
| Destination integrity | Retry keeps the task destination. | Task persists `destinationId`; Worker reloads task; engine/client use it. | PASS — repository verified |
| File integrity | Source URI remains the task source. | Worker reloads persisted URI; engine stages that URI. | PASS — repository verified; provider runtime unverified |
| Account integrity | Logout/re-login does not corrupt queued items. | Session cache clears; queued tasks remain in Room. | NOT VERIFIED |
| Notification consistency | Notifications reflect queue state when implemented. | No upload notification/foreground service path was found. | NOT APPLICABLE |
| Process recovery | WorkManager/Room and UI converge after process death. | Architecture supports persisted task lookup; no device test or reconciliation code found. | NOT VERIFIED |

## Known Automated-Test Gaps

A complete future test phase should cover status-writer ordering around pause/cancel, progress after terminal state, replacement while a Worker is running, unknown persisted status, missing task/source/destination, process recreation, WorkManager worker recreation, network interruption, and UI progress representation. Tests should use controlled doubles only at repository/engine boundaries and should not replace real TDLib code in production.

## Runtime Evidence Rule

A successful compile or unit-test run is not runtime certification. Real Telegram delivery, process death, session interactions, and Android WorkManager execution require a connected device or emulator. If unavailable, the correct result is **UPLOAD STATE RUNTIME VERIFICATION NOT AVAILABLE**.
