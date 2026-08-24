# Upload Scheduler Inventory

**Scope:** Scheduled upload creation, persistence, WorkManager handoff, duplicate execution, cancellation, restart recovery, time representation, file/destination/account references, and UI ownership.

| Component | Location | Actual data/behavior | Owner | Persistence | Risk |
|---|---|---|---|---|---|
| Schedule input | `UploadViewModel` / `UploadScreen` | Optional `scheduledAt: Long?` timestamp in milliseconds | Upload ViewModel | Transient until task insertion | User can change the scheduled timestamp before enqueue. |
| Scheduled upload task | `UploadTask` / `UploadEntity` | `scheduledAt` plus normal source URI, destination ID, status, and metadata | Room record | Yes | Schedule is embedded in upload task; no separate schedule entity. |
| Delay calculation | `UploadViewModel.addToQueue` | `max(scheduledAt - System.currentTimeMillis(), 0)` | Upload ViewModel | No separate state | Past timestamps run with zero delay; invalid timestamp validation was not identified. |
| Work request | `UploadManagerImpl` | One-time request with initial delay, connected-network constraint, task-ID input | WorkManager | WorkManager database | No separate scheduler queue. |
| Unique work identity | `UploadManagerImpl` | Unique work name equals upload task ID | WorkManager | WorkManager database | Normal enqueue uses KEEP; retry/resume use REPLACE. |
| Schedule cancellation | Queue ViewModel / Upload Manager | Pause/cancel call `cancelUniqueWork(id)` | WorkManager + Room status | Room status and WorkManager | Cancellation is asynchronous and not a separate schedule lifecycle. |
| Schedule restoration | Existing upload/manager paths | No explicit startup schedule reconciler found | WorkManager/Room | Implicit platform persistence | Device/process restart behavior not runtime-tested. |
| Scheduler UI | Upload screen | Allows selecting/changing a timestamp and shows scheduled state | Compose UI | No separate schedule UI model | No dedicated schedule list/edit/cancel feature identified. |
| File reference | Upload task | Persisted `sourceUri` | Room | Yes | URI permission/provider behavior after delay is unverified. |
| Destination reference | Upload task | Persisted `destinationId: Long` | Room | Yes | Scheduled work does not read the currently selected UI destination after insertion. |
| Account reference | Telegram client/session | Single TDLib account session | TDLib client | TDLib private directory | Logout/re-login interaction with queued schedules is unverified. |

## Scheduler Semantics

Scheduling is a one-time delay attached to each upload task, not a recurring calendar scheduler. There is no separate schedule ID, enabled flag, next-execution record, or schedule repository in the reviewed source. A scheduled upload is inserted into Room and then passed to WorkManager with an initial delay.

No duplicate schedule persistence system was found. Repeated normal enqueue for the same task uses WorkManager unique work with `KEEP`; replacement is used for manual retry or resume. No runtime test of repeated schedule taps, device restart, clock changes, or two concurrent execution attempts was available.
