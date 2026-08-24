# History and Scheduler Architecture

## History Authority

There is one durable upload record in Room. History is not a second table or repository: `HistoryViewModel` observes `UploadRepository.getAllUploads()`, filters to `COMPLETED`, applies period/name filters, and sorts the resulting list. `UploadWorker` creates the completed state by updating the same upload record after confirmed engine success. This avoids duplicate history insertion and gives queue and history a shared durable source.

## History Lifecycle

Active uploads remain in the `uploads` table but are not shown by the history projection. A completed record becomes visible when its status is `COMPLETED`. Failed, cancelled, paused, retrying, queued, and preparing records do not appear in history. Single deletion removes the selected Room record, and clear history deletes only rows whose status is `COMPLETED`.

The default history order is newest first using `completedAt` when present and `createdAt` as fallback. Largest-first sorting uses `fileSize`. There is no pagination query; the full Room Flow is filtered and sorted in memory.

## Scheduler Authority

The application has no separate scheduler entity or scheduler repository. Optional `scheduledAt` is stored on the upload task itself. `UploadViewModel` inserts the task, calculates a nonnegative initial delay, and asks `UploadManagerImpl` to create one WorkManager request. WorkManager owns execution scheduling; Room retains the schedule timestamp as part of the upload record.

Unique work names are task IDs. Normal enqueue uses `KEEP`, while retry/resume use `REPLACE`. This is the current duplicate-execution control. No recurring schedule, schedule editing store, enabled flag, or startup reconciliation routine was found.

## File, Destination, and Account References

A scheduled task captures the source URI and destination ID at insertion time. Later Worker execution reloads those task fields from Room rather than consulting current UI selection. File-provider access after a long delay and Telegram session changes after scheduling remain runtime validation items.

## Notifications

No upload notification architecture exists in the reviewed source. Queue and history are Room-backed UI projections; notification state is not a second view that can disagree with them.

## Boundaries

No history schema, schedule schema, WorkManager replacement, TDLib change, notification feature, or automatic cleanup was introduced by this review.
