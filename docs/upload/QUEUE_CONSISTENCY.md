# Queue Consistency

## Durable Record

Room stores one record per upload keyed by the stable string `id`. The record includes source URI, file metadata, destination ID, status, progress counters, speed/ETA, timestamps, retry count, scheduled time, and upload duration. The repository maps these fields directly between Room and `UploadTask` without creating another state authority.

## Queue Projection

`QueueViewModel` observes all uploads and derives queue items from Room. Completed and cancelled records are excluded from the pending queue. Active filtering includes queued, preparing, uploading, and retrying. Paused and failed filters are separate. The list is ordered by the DAO’s `createdAt` ordering; no manual reorder or priority system was identified.

## Worker Synchronization

The Worker receives only `upload_id`, reloads the current `UploadTask` from Room, and updates Room as engine events arrive. This avoids dependence on mutable UI state. WorkManager unique work is named with the task ID. Normal enqueue uses `KEEP`; retry and resume use `REPLACE`. These choices reduce ordinary duplicate chains while leaving asynchronous cancellation/replacement ordering as a runtime risk.

## Scheduler and Restart

Scheduled tasks retain `scheduledAt` in Room and are re-enqueued with a calculated delay on resume. No explicit process-death reconciliation or stale WorkManager-to-Room repair was found. A normal restart should rely on WorkManager and Room, but this was not tested on a device in this phase.

## Destination and File Integrity

The destination ID is persisted per task and is passed through the Worker to the upload engine. The source URI is persisted per task and is staged by the engine before TDLib handoff. No code path intentionally substitutes another destination or source file. Cross-session destination validity and provider behavior remain unverified.

## History and Notifications

History displays completed uploads only. Failed, cancelled, queued, paused, retrying, and active records are not shown in the history projection. No upload notification implementation, `setForeground`, or `ForegroundInfo` path was found in the reviewed source; therefore notification-state consistency is not applicable to an implemented upload notification feature.

## Queue Data Loss Boundaries

Manual removal cancels unique work and then deletes the Room record. Clearing history deletes completed records. A failure between cancellation and deletion, or a late Worker write after deletion, was not covered by a transaction or runtime test. No automatic deletion of failed, unknown, or orphaned records was found.

## Decision

Room-backed projection and task-ID-based WorkManager identity provide a coherent normal path. Formal atomic transitions, cancellation fencing, process-death reconciliation, and unknown-state repair are not implemented and remain documented risks.
