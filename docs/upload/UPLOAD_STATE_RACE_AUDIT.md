# Upload State Race Audit

## State Authority

Room is the durable application authority for upload status, progress, retry count, file source, destination, and timing fields. The queue UI observes repository/Room flows. WorkManager is the execution scheduler, while the Worker writes execution outcomes back to Room. TDLib is authoritative for Telegram transfer and message-delivery events, but it does not directly own the persisted application status.

## Writers

| Writer | Fields written | Race consideration |
|---|---|---|
| Upload preparation | Inserts task with initial status and metadata. | Enqueue follows insertion; a failure between operations can leave an unqueued record. |
| Queue ViewModel pause | `status = PAUSED`, then cancels unique work. | A late progress callback can set `UPLOADING` again. |
| Queue ViewModel resume | `status = QUEUED`, then re-enqueues with replacement policy. | Existing worker cancellation/replacement ordering is asynchronous. |
| Queue ViewModel retry | `status = RETRYING`, then re-enqueues with replacement policy. | A prior worker may still be settling. |
| Queue ViewModel cancel | `status = CANCELLED`, then cancels unique work. | A late progress/completion callback may overwrite cancellation. |
| Worker start | `status = PREPARING`. | UI actions can race with worker execution. |
| Worker progress | Bytes, speeds, ETA, and `status = UPLOADING`. | The unconditional status assignment is the strongest verified race risk. |
| Worker success | Duration and `status = COMPLETED`. | A late progress event could theoretically write uploading afterward. |
| Worker failure | `RETRYING` or `FAILED`. | Retry/replacement and prior worker completion can overlap without a transition guard. |
| Delete action | Deletes Room record after cancelling WorkManager. | In-flight worker writes after deletion are not runtime verified. |

## Multi-Writer Risk

The implementation has multiple legitimate writers and no central compare-and-set transition guard. `UploadDao.updateStatus()` accepts a new status without checking the prior state. `UploadDao.updateProgress()` writes `UPLOADING` regardless of the current state. This allows a callback that was already emitted before pause/cancel to win a later database write.

No evidence was found that UI state is maintained as an independent fake queue. The UI is projected from Room, which reduces long-lived UI/DB divergence. The remaining risk is write ordering between queue actions, Worker callbacks, retry replacement, and cancellation.

## WorkManager Duplication

Normal enqueue uses `enqueueUniqueWork(task.id, KEEP, request)`, which prevents a second normal work chain with the same task ID from replacing the existing chain. Manual retry and resume use `REPLACE`, intentionally replacing existing unique work. This reduces ordinary duplicate execution but does not by itself prove that a previously running Worker has stopped before replacement.

## Progress Consistency

The engine clamps uploaded bytes to the total and computes percentage in the range 0..100. The DAO stores that percentage directly. The status indicator clamps the progress-bar value to 0..1 but renders the textual percentage as `video.progress * 100`; consequently, a stored value such as 50 is displayed as 5000% while the bar is clamped to full. This is a verified representation mismatch and a maintenance bug boundary. It was documented, not corrected, because the supplied protocol forbids unapproved behavior changes.

## Retry and Cancellation

Retry is bounded by `MAX_RETRY_ATTEMPTS = 5` in the Worker. Retryable engine errors cause `Result.retry()` while the attempt limit has not been reached; otherwise the task becomes `FAILED`. Manual retry writes `RETRYING` and replaces unique work. Pause and cancel write Room first and cancel unique work second. There is no automatic timeout and no indefinite retry path in the reviewed code.

## Recovery and Unknown States

Missing upload records cause Worker failure. Completed records are skipped. Missing source/destination, unauthenticated TDLib, staging errors, and TDLib failures flow through the engine/Worker error path. No explicit orphan-record repair, stale-state migration, unknown-enum recovery, process-death reconciliation, or stuck-upload detector was found. These are limitations, not silently repaired states.

## Safety Decision

The queue has a coherent durable record and stable upload identity, but it is not a formally serialized state machine. The progress/status race and representation mismatch require follow-up evidence and controlled fixes in a future phase. No schema change, state reset, timeout, or upload-engine redesign was introduced.
