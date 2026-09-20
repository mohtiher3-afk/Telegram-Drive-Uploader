# Upload History Inventory

**Scope:** Completed-upload history, record ownership, filtering, sorting, deletion, retry visibility, privacy, and consistency with the queue.  
**Review mode:** Documentation-only controlled maintenance; no history schema or upload behavior changed.

| Component | Location | Data | Owner | Persistence | Risk |
|---|---|---|---|---|---|
| Upload record | `data/local/database/UploadEntity.kt` | ID, source URI, file metadata, destination ID, status, progress, timing, error, retry count, media metadata, schedule, duration | Room | `uploads` table | One record serves both active queue and completed-history projection. |
| Upload domain task | `domain/model/UploadTask.kt` | Direct domain representation of the persisted record | Repository mapper | No separate storage | Stable identity is `id`; no duplicate history model. |
| DAO all-upload query | `data/local/database/UploadDao.kt` | All records ordered by `createdAt DESC` | Room | Database Flow | History loads the full query before in-memory filtering. |
| History repository path | `data/repository/UploadRepositoryImpl.kt` | Direct DAO/domain mapping | Repository | Room | No second history authority. |
| History ViewModel | `feature/history/HistoryViewModel.kt` | Completed records, query, period, sort, total count, total size | ViewModel projection | Reads Room | Failed/cancelled/active records are excluded. |
| History screen | `feature/history/HistoryScreen.kt` | User-facing completed items and delete/clear actions | Compose UI | No direct persistence | Visual/runtime accessibility and large-history behavior not device-tested. |
| Single deletion | `HistoryViewModel.deleteUpload` | Record ID | History ViewModel → repository | Deletes one Room record | No active-state guard in this method; UI normally exposes it for history items. |
| Clear history | `HistoryViewModel.clearHistory` | All records with `COMPLETED` status | History ViewModel → DAO | Deletes completed records only | Does not delete active, failed, paused, retrying, or cancelled rows. |
| Completion writer | `UploadWorker` | `uploadDurationMs`, `COMPLETED` status | Worker | Room | No separate history insert, reducing duplicate-history risk. |

## Actual History Semantics

History is a filtered view over the same `uploads` table rather than a second persistence system. Only records whose current status is `COMPLETED` appear. The default ordering is newest first by `completedAt ?: createdAt`; an alternate largest-first sort uses `fileSize`. Date periods use local `Calendar` boundaries for TODAY and epoch-millisecond subtraction for the seven- and thirty-day filters.

No retry-from-history action, separate history status, destination title persistence, notification record, or history pagination query was identified.

## Privacy Boundary

History displays file metadata already present in the task model. The reviewed diagnostics do not intentionally log private filenames or destination names for history events. The history record stores source URI and destination ID as required by the upload record; it does not store message content or Telegram credentials.
