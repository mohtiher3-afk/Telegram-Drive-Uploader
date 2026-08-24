# Upload Recovery

## Implemented Recovery Behavior

A missing upload record causes the Worker to return failure. A completed task is skipped. Retryable engine failures return `Result.retry()` while the Worker attempt count is below five; otherwise the task is marked failed. Manual retry and resume update the Room state and re-enqueue unique work. Cancellation and pause cancel unique work after writing the requested Room status.

A stream that ends without a terminal engine event is marked failed by `UploadCompletionPolicy`. This prevents a Worker reaching the end of its collection from being interpreted as Telegram success. The engine deletes its staged temporary file in `finally`.

## Recovery Cases Not Implemented or Not Verified

| Case | Current behavior/evidence | Status |
|---|---|---|
| Process death during upload | WorkManager/Room architecture is present, but no device test or reconciliation logic was identified. | NOT VERIFIED |
| Worker recreation | Worker reloads by `upload_id`; runtime recreation was not exercised. | NOT VERIFIED |
| Network interruption | Retryable exceptions and Telegram error paths exist; no dedicated network-restoration state machine was found. | NOT VERIFIED |
| Missing source file | Engine staging failure becomes an error; exact UI/retry behavior depends on Worker classification. | CONDITIONALLY SUPPORTED |
| Missing destination | Engine rejects `destinationId == 0`; inaccessible nonzero destinations rely on TDLib failure. | CONDITIONALLY SUPPORTED |
| Orphaned Room record | No automatic orphan scanner or deletion was found. | NOT IMPLEMENTED |
| Unknown persisted state | No explicit safe mapping or migration path was found. | NOT VERIFIED |
| Stuck `UPLOADING` | No progress watchdog or timeout detector was found. | NOT IMPLEMENTED |
| Pause/cancel race | Late callbacks can write progress/status after the UI action. | VERIFIED RISK |
| Queue data loss on removal | Cancel then delete is sequential, not a transaction. | RUNTIME RISK |

## No Speculative Changes

The audit did not add a timeout, queue reset, schema migration, cancellation fence, process-death repair, or alternate Worker system. Those changes require a separate approved implementation phase based on reproducible evidence.
