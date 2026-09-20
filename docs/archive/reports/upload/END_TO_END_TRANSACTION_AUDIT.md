# End-to-End Transaction Identity and Integrity Audit

## Stable Identity

| Identity concept | Actual implementation | Decision |
|---|---|---|
| Business upload ID | `UploadTask.id` | Stable application identity. |
| Database identity | Same task ID / Room primary key | One persisted record per upload. |
| Queue identity | Same task ID in queue list key | Stable UI key. |
| Worker input | `Data["upload_id"]` | Minimal durable Worker input; Worker reloads the task. |
| WorkManager identity | Unique work name `task.id`; internal request UUID also exists | Unique work policy is keyed by business task ID. |
| Destination identity | `destinationId: Long` | Captured on task creation and used for TDLib send. |
| File identity | `sourceUri: String` plus file metadata | URI reference, not filename-only routing. |
| Account identity | Not persisted per task | Single-account assumption; stale-session behavior is unverified. |

The implementation does not use filename, list index, chat title, or current UI selection as the sole identity of an upload.

## Input Snapshot

The upload task captures the source URI, display filename, MIME type, total bytes, duration, dimensions, destination ID, optional schedule timestamp, and initial status. The account/session is held by the singleton TDLib client rather than copied into the task. There is no second snapshot store and no per-task account ID.

## Destination Integrity

`UploadViewModel.addToQueue` copies the selected destination ID into every task before Room insertion. `UploadManagerImpl` passes only the task ID to WorkManager. `UploadWorker` reloads the task and the engine/client uses the task destination ID. The selected destination is therefore decoupled from later UI changes. A stale or inaccessible destination after logout/re-login requires a real-session test.

## File Integrity

Metadata extraction starts from the selected SAF URI and returns a task with the same source URI. The task is persisted before Worker execution. The Worker reloads that URI, and the upload engine stages the source content before passing a local path to TDLib. No filename-only substitution or current-picker lookup was found. Provider revocation, replacement content at the same URI, and large-file process-death behavior are not verified.

## Queue Creation Window

For each prepared task, the current sequence is:

`copy destination/schedule → insert Room record → enqueue unique WorkManager request → log enqueue`

If Room insertion fails, the current coroutine stops and the task is not enqueued. If Room insertion succeeds but WorkManager enqueue fails or is rejected, a persisted `QUEUED` record may remain without runnable work. This is a documented failure window; no reconciliation or automatic reset was added.

## Worker Duplication

Ordinary enqueue uses unique work with `KEEP`, task ID as unique name, and one-time request semantics. Retry and resume intentionally use `REPLACE`. This reduces duplicate ordinary Workers but does not by itself prove that an already-running replaced/cancelled Worker cannot emit a late status write. That race remains unverified and is documented in the earlier upload-state audit.

## Account Integrity

The repository assumes one TDLib account/session. The Worker does not carry an account ID and relies on the current singleton client/session. Logout and re-login are not linked to task invalidation. Therefore wrong-account or unauthenticated execution after a session change cannot be ruled out statically and requires controlled runtime evidence.

## Terminal Integrity

The Worker marks completion only after the upload engine returns success. The engine waits for the real Telegram send-success signal rather than treating Worker completion as Telegram completion. Late progress/status callbacks can still race with cancellation or terminal state because status writes are not centrally fenced.
