# Upload State Inventory

**Scope:** Upload lifecycle, queue consistency, Worker synchronization, retry/cancel behavior, persistence, progress, completion, and recovery.  
**Mode:** Production / Controlled Maintenance Mode.  
**Change policy:** Documentation-only review; no upload architecture, TDLib behavior, database schema, or WorkManager implementation was changed.

## Components

| State/Class | Location | Owner | Persisted | UI | Worker | TDLib | Risk / Review Note |
|---|---|---|---|---|---|---|---|
| `UploadStatus` | `domain/model/UploadStatus.kt` | Domain enum | As string in Room | Queue/history state rendering | Worker branches on status | Indirectly reflected by engine events | Actual states include `QUEUED`, `PREPARING`, `UPLOADING`, `PAUSED`, `RETRYING`, `COMPLETED`, `FAILED`, and `CANCELLED`. |
| `UploadTask` | `domain/model/UploadTask.kt` | Domain model | Mapped from Room | Queue/history | Loaded by Worker | Passed to engine/client | Stable upload identity is `id: String`; destination is `Long`. |
| `UploadEntity` | `data/local/database/UploadEntity.kt` | Room schema | Yes | Indirectly | Reloaded by Worker | Stores source/destination metadata used by upload | Several timestamps/error/retry fields exist but are not actively written in the reviewed status paths. |
| `UploadDao.updateStatus` | `data/local/database/UploadDao.kt` | Room write boundary | Yes | Flows update UI | Called by queue ViewModel and Worker | None | Unconditional status replacement permits callers to request transitions without a central transition validator. |
| `UploadDao.updateProgress` | `data/local/database/UploadDao.kt` | Room write boundary | Yes | Flows update UI | Called for engine progress | Derived from TDLib progress | Always writes `status = 'UPLOADING'`, creating a verified race/contradiction risk with pause/cancel/failure writers. |
| `UploadWorker` | `feature/upload/worker/UploadWorker.kt` | WorkManager execution | Updates Room | Queue observes Room | Owns preflight, result handling, retry result | Receives engine events indirectly | Completion requires terminal engine success; process/restart runtime behavior is not verified. |
| `UploadManagerImpl` | `data/upload/UploadManagerImpl.kt` | Work scheduling | WorkManager metadata plus Room task | Queue actions call it | Enqueues/cancels unique work | None | Unique work name is task ID; normal enqueue uses KEEP, retry/resume uses REPLACE. |
| `TelegramUploadEngineImpl` | `data/upload/TelegramUploadEngineImpl.kt` | Engine boundary | No direct state write | Worker consumes results | Emits progress/success/error | Delegates real TDLib events | Emits success only after `TelegramUploadEvent.Completed`; progress percentage is 0..100. |
| Queue ViewModel | `feature/queue/QueueViewModel.kt` | User actions and queue filters | Calls repository | Filters active/paused/failed | Calls manager | None | Pause/cancel update Room before cancelling WorkManager, creating an ordering race with progress updates. |
| Queue UI indicator | `core/ui/components/UploadStatusIndicator.kt` | UI renderer | No | Status, progress, pause/resume/retry/cancel | No | No | Progress bar clamps to 0..1 while percent text multiplies by 100; persisted engine percentage is 0..100, a verified representation mismatch. |
| History ViewModel | `feature/history/HistoryViewModel.kt` | History projection | Reads Room | Shows completed only | No | No | Failed/cancelled/active/paused/retrying records are excluded from history UI. |
| Completion policy | `domain/upload/UploadCompletionPolicy.kt` | Terminal-event guard | No | Indirectly | Worker uses it | Confirms engine stream terminal event | Prevents stream exhaustion without a terminal event from becoming success. |

## Identity and Persistence

Each upload is identified by a generated string ID, not by filename. Room stores the source URI, file metadata, destination ID, status, progress counters, timing fields, retry count, and upload duration. WorkManager receives only `upload_id` and reloads the task from Room. No second queue database or fake upload state exists in the reviewed path.

## Evidence Boundary

This inventory is based on repository inspection. It does not claim process-death, device, network, or real Telegram runtime verification.
