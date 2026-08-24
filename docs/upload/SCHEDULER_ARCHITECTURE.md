# Scheduler Architecture

## Current Model

The app implements one-time scheduled uploads by storing an optional epoch-millisecond `scheduledAt` on each `UploadTask`/`UploadEntity`. It does not implement a separate recurring scheduler or schedule table.

## Creation Path

The upload UI changes the transient timestamp in `UploadViewModel`. When the user adds the prepared files to the queue, each task is copied with the selected destination and the same `scheduledAt` value, inserted into Room, and submitted to `UploadManagerImpl`. The manager creates a `OneTimeWorkRequest` with an initial delay and a connected-network constraint.

## Identity and Duplication

The WorkManager unique-work name is the stable task ID. Normal enqueue uses `ExistingWorkPolicy.KEEP`, so repeating ordinary enqueue does not replace or duplicate an existing chain. Retry and resume intentionally use `REPLACE`. No separate schedule ID or duplicate schedule store exists.

## Cancellation and Editing

There is no dedicated schedule-cancel or schedule-edit repository. Queue pause/cancel operations cancel the task’s unique WorkManager work and update the upload status. Changing a scheduled timestamp is a new transient preparation choice; no existing-schedule edit flow was found.

## Recovery and Time

The delay is calculated from `scheduledAt - System.currentTimeMillis()` and clamped at zero. Past-due schedules therefore run without delay. The timestamp is stored as epoch milliseconds; the UI picker’s local display and behavior after timezone/DST/clock changes were not device-tested. WorkManager persistence may support restart restoration, but no explicit app reconciliation was found.

## Safety Boundaries

A schedule captures its file URI and destination ID in the upload task before Worker execution. It does not consult the current UI destination later. Missing/revoked URI access, destination/session changes, process death, device restart, and duplicate execution require runtime evidence. No scheduling semantics were changed.
