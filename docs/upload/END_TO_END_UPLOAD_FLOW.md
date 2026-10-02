# End-to-End Upload Flow

**Scope:** One upload transaction from authenticated user action through Telegram result, persisted status, history, notification boundary, and UI.  
**Mode:** Controlled maintenance; documentation-only review.

## Complete Flow

`User → authenticated TDLib session → destination selection → URI/file preparation → UploadTask → Room → WorkManager → UploadWorker → TelegramUploadEngine → TelegramClient/TDLib → Telegram success/failure → Room status → queue/history UI`

No upload notification stage exists in the reviewed implementation.

| Arrow | Source | Target | Data / identifier | State | Failure path |
|---|---|---|---|---|---|
| User → session | Telegram auth UI | TDLib client | Phone/code/2FA or QR input handled by auth flow | Authorization state | TDLib/auth error shown; runtime proof unavailable. |
| Session → destination | Telegram repository/client | Destination ViewModel/UI | TDLib chats and stable `chat.id` | Authorized | Empty/error destination state; chat visibility runtime unverified. |
| Destination → file prep | Upload UI | Upload ViewModel | Selected `TelegramDestination.id` | Prepared | Missing destination prevents queue insertion. |
| File → task | SAF picker | Metadata extractor | URI, name, size, MIME, duration, dimensions | `QUEUED` initial task | Metadata/source exception yields error; no substitution path identified. |
| Task → Room | Upload ViewModel | Upload repository/DAO | Full task keyed by `task.id`, including URI and destination ID | `QUEUED` | DB failure is logged; queue insertion sequence may stop before later tasks. |
| Room → WorkManager | Upload Manager | Unique work | `upload_id = task.id`, initial delay, network constraint | Scheduled/ENQUEUED | WorkManager failure after DB insert is a known failure window. |
| WorkManager → Worker | WorkManager | UploadWorker | Only `upload_id`; Worker reloads Room task | Running | Missing task fails; process/restart behavior not device-tested. |
| Worker → engine | UploadWorker | TelegramUploadEngine | Reloaded URI, destination ID, file metadata | `PREPARING`/`UPLOADING` | Preflight/source/session/TDLib error becomes retryable or terminal failure. |
| Engine → TDLib | Engine/client | TDLib | Local staged file path, destination ID, message content | Uploading | TDLib error or unconfirmed result becomes failure/retry. |
| TDLib → Worker | TelegramClient | Engine/Worker | Completion event / result | Success or failure | Worker does not equate mere Worker completion with Telegram success. |
| Worker → Room | Worker | Repository/DAO | Status, progress, error, duration, completion time | Terminal or retrying | Late callbacks and progress writes remain a known race risk. |
| Room → queue | DAO Flow | Queue ViewModel/UI | Same task record | Filtered current queue | UI reflects persisted state; device runtime unverified. |
| Room → history | DAO Flow | History ViewModel/UI | Completed records only | `COMPLETED` projection | Failed/cancelled records excluded. |
| Room → notification | None | None | No notification implementation | Not applicable | No stale-notification path exists because notifications are absent. |

## Transaction Identity

The stable identity is `UploadTask.id`, persisted as the Room record ID and passed to WorkManager as `upload_id`. The same ID is used for unique work naming, Worker reload, repository observation, diagnostic correlation, and queue item keys. Filename, list index, chat title, and current UI selection are not the transaction identity.

No separate `queueId`, `workerId`, or `databaseId` was found beyond the task/Room ID and WorkManager's generated internal request ID. The WorkManager request ID is not passed to the upload engine or persisted as the business transaction identity.

## Input Snapshot

At creation, the task captures the source URI, display filename, MIME type, byte size, duration, dimensions, selected destination ID, optional schedule timestamp, and initial `QUEUED` status. The app has one TDLib account/session; no account ID is persisted with each upload. The current UI destination is not read after task insertion.

## Protected Invariants

The intended file and destination references are carried by the same persisted task into the Worker and upload engine. Completion requires an actual successful engine result, and no notification layer can diverge because it is not implemented. Runtime invariants after process death, account change, provider revocation, or network loss remain unverified.
