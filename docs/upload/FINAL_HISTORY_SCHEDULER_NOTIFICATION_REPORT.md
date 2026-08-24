# Final History, Scheduler, and Notification Consistency Report

**Repository:** [mohtiher3-afk/Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)  
**Mode:** Production / Controlled Maintenance Mode  
**Scope:** Upload history, scheduled uploads, WorkManager scheduling, notifications, deletion, sorting, persistence, privacy, and consistency with queue and Worker.  
**Change policy:** Documentation-only. No database schema, TDLib, upload engine, WorkManager, or notification feature was changed.

## History Architecture

History is a projection over the same Room `uploads` table used by the queue. `HistoryViewModel` observes `UploadRepository.getAllUploads()`, filters to `UploadStatus.COMPLETED`, applies date and filename filters, and sorts the result. There is no separate history entity, history repository, or completion-history insertion path.

## History State

Only completed records appear in history. Failed, cancelled, queued, preparing, uploading, paused, and retrying records remain ordinary upload records but are excluded from the history projection. The default order is newest first by `completedAt ?: createdAt`; the alternate order is largest first by `fileSize`. History filters use local midnight for TODAY and epoch-millisecond subtraction for seven- and thirty-day periods.

## Duplicate History Risk

The stable upload ID is the Room primary identity, and completion updates the existing record rather than inserting a second history record. This reduces duplicate-history risk during retry or repeated completion handling. A concurrent late Worker write can still affect the record’s final status because state transitions are not centrally fenced; no duplicate-history runtime test was available.

## Retry From History

No retry-from-history action was found. Retry is exposed from the queue for failed tasks. Therefore the history-to-retry path is not implemented and no new history record is created by history UI.

## History Deletion

Single history deletion removes the selected Room record. Clear history calls the DAO operation that deletes rows with `COMPLETED` status only. It does not directly cancel active WorkManager work or delete active queue items. The history UI normally displays completed records, but the single-delete repository method itself has no explicit status guard. Runtime interaction with concurrent work was not tested.

## Scheduler Architecture

Scheduling is a one-time initial delay attached to each upload task. There is no separate schedule table, schedule ID, enabled flag, recurring scheduler, or schedule repository. `UploadViewModel` captures an optional `scheduledAt` epoch-millisecond timestamp, copies it into each task, persists the task, calculates a nonnegative delay, and passes it to `UploadManagerImpl`.

## Schedule Persistence

The schedule timestamp is persisted in the same Room upload record as source URI, destination ID, file metadata, status, and retry data. WorkManager receives only `upload_id` plus its request configuration and reloads the task through the Worker. The current UI destination is not consulted later, so the scheduled task retains the destination selected at insertion time.

## Duplicate Schedule Risk

Normal enqueue uses `enqueueUniqueWork(task.id, ExistingWorkPolicy.KEEP, request)`, reducing duplicate ordinary execution for one task. Retry and resume use `REPLACE` intentionally. No separate duplicate schedule store was found. Tapping schedule twice, process restart, replacement during execution, and device restart were not runtime-tested, so duplicate execution is not fully ruled out.

## Schedule Recovery

WorkManager and Room provide the persistence basis for delayed execution and Worker recreation. No explicit application startup reconciler, orphan schedule detector, or stale WorkManager repair routine was identified. Past-due timestamps use zero delay. Missing/revoked file access, process death, network interruption, and session changes after scheduling remain unverified.

## Time Handling

`scheduledAt` is stored as epoch milliseconds. Delay is computed as `scheduledAt - System.currentTimeMillis()` and clamped to zero. History uses local `Calendar` boundaries for TODAY, while relative history windows use epoch arithmetic. Timezone changes, DST transitions, device clock changes, invalid timestamps, and UI display behavior were not tested. No persisted time representation was changed.

## File Reference

Each scheduled task captures its source URI in Room before Worker execution. The Worker reloads that task, and the upload engine stages the same source URI. A delayed upload can encounter revoked permission, deleted content, or unavailable provider; the current error path is documented, but provider/runtime behavior was not tested.

## Destination Reference

Each scheduled task captures `destinationId: Long` before enqueue. Worker execution reloads that ID from Room and passes it through the upload engine to TDLib. The schedule does not use the current UI destination after insertion. Stale or inaccessible destinations after account/session changes remain runtime validation items.

## Account State

The app uses a single TDLib account/session. Logout clears in-memory Telegram state, while existing Room upload records and scheduled timestamps remain. The repository does not automatically invalidate or rewrite queued schedules after logout. Whether a scheduled task safely waits for re-authentication or fails under a changed session was not tested with a real account.

## Notification Architecture

No upload notification channel, notification builder, stable notification ID, progress notification, completion/failure notification, notification action, foreground service, `setForeground`, or `ForegroundInfo` path was found in the reviewed source or manifest. Queue and history are Room-backed UI projections; notifications are not a second state authority.

## Notification Identity

No notification identity exists because no upload notification is implemented. Consequently, notification overwrite or duplicate-notification behavior is not applicable.

## Notification State Mapping

There is no implemented mapping from queued, preparing, uploading, paused, completed, failed, or cancelled to system notifications. These states are represented in Room and queue/history UI only.

## Notification Actions

No notification pause, resume, cancel, retry, or open actions exist. The corresponding controls, where available, are queue UI actions connected to `QueueViewModel` and `UploadManager`.

## Privacy

No upload notification content exists to expose filenames, destination names, message content, or authentication information on the lock screen. History and upload diagnostics use the existing local record and masked diagnostic conventions. No credential or message-content notification path was introduced.

## Performance

History reads all uploads through one Room Flow and filters/sorts in memory. There is no pagination query or dedicated large-history projection. The performance impact of a very large history remains unmeasured. Scheduling uses one WorkManager request per task and no recurring scheduler loop was identified.

## Tests

The test matrix records repository-supported completion/history projection, completed-only deletion, timestamp persistence, unique WorkManager naming, destination/file capture, and notification absence. Runtime cases for restart, device restart, clock changes, network loss, delayed URI access, session changes, duplicate taps, and actual Telegram delivery remain unverified. No automated tests were added because this was a documentation-only phase and the protocol forbids speculative feature changes.

## Runtime Verification

**HISTORY SCHEDULER NOTIFICATION RUNTIME VERIFICATION NOT AVAILABLE.** No connected device or emulator with a real Telegram session was available for completion, failure, retry, cancellation, scheduled execution, restart recovery, file-provider persistence, account changes, or notification behavior.

## Known Limitations

The app has no separate scheduler model, recurring schedules, explicit schedule reconciliation, stale-session invalidation, schedule edit repository, history pagination, notification feature, notification permission flow, or notification state machine. These are documented boundaries, not missing behavior silently replaced in this review.

## Remaining Risks

The primary risks are WorkManager replacement/cancellation races, delayed file-provider access, scheduled task execution after account/session changes, unverified restart behavior, full-history in-memory processing, and untested clock/timezone transitions. Adding notifications or altering schedule persistence would require a separate approved feature or schema phase.

## Final Safety Check

| Check | Decision |
|---|---|
| Duplicate history possible | UNKNOWN — one Room record reduces risk; concurrent state races unverified |
| Duplicate schedules possible | UNKNOWN — unique work reduces ordinary duplication; runtime replacement/restart unverified |
| Duplicate notifications possible | NOT APPLICABLE — no notification implementation |
| Notification can disagree with upload | NOT APPLICABLE — no notification implementation |
| History can disagree with upload | UNKNOWN — shared Room record reduces projection divergence; late status writers remain a known risk |
| Schedule can use wrong destination | UNKNOWN in stale/session cases; task-level destination capture verified |
| Schedule can use inaccessible file | YES as a possible provider/runtime condition; error handling exists but runtime outcome unverified |
| Schedule survives restart | NOT VERIFIED |
| Notification privacy issue | NO notification path exists |
| TDLib changed | NO |
| Upload semantics changed | NO |
| Database schema changed | NO |

## Final Decision

# HISTORY SCHEDULER NOTIFICATION CONDITIONALLY VERIFIED

History and scheduling use the existing Room upload record and task-ID-based WorkManager path without duplicate persistence systems. Completed history is derived from confirmed task status, and scheduled tasks retain their file and destination references. Full certification is blocked by unavailable device/runtime evidence, unverified restart and account/session behavior, and the fact that upload notifications are not implemented.

## References

[1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader "Telegram Drive Uploader repository"
[2]: https://developer.android.com/topic/libraries/architecture/workmanager "Android WorkManager documentation"
[3]: https://developer.android.com/training/data-storage/room "Android Room documentation"
[4]: https://developer.android.com/develop/ui/views/notifications "Android notifications documentation"

PHASE AK COMPLETE — HISTORY, SCHEDULER AND NOTIFICATION CONSISTENCY REVIEW COMPLETE — WAITING FOR APPROVAL
