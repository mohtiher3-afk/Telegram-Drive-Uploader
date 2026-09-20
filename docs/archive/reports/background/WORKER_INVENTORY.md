# Worker Inventory

## UploadWorker

| Property | Actual behavior |
|---|---|
| Type | Hilt-assisted `CoroutineWorker` |
| Input | String `upload_id` from WorkManager input data |
| Lookup | Reloads the upload task from `UploadRepository` |
| Preconditions | Missing task returns `Result.failure()`; already `COMPLETED` task returns `Result.success()` |
| Initial state | Persists `PREPARING` before the engine handoff |
| Progress | Persists uploaded bytes, total bytes, percentage, current speed, average speed, and ETA from engine progress events |
| Success | Requires a terminal engine success result, persists duration and `COMPLETED`, returns `Result.success()` |
| Unconfirmed stream | A stream ending without a terminal result is marked `FAILED` and returns `Result.failure()` |
| Retryable error | Persists `RETRYING` and returns `Result.retry()` while `runAttemptCount < 5` |
| Permanent error | Persists `FAILED` and returns `Result.failure()` after the retry boundary or for non-retryable engine errors |
| Exception | Maps and logs the exception, persists retrying/failed state, and returns retry/failure according to the attempt boundary |
| Backoff | Work request uses exponential backoff with an initial delay of 30 seconds |
| Constraint | `NetworkType.CONNECTED` |
| Cancellation | Manager cancellation calls `cancelUniqueWork(id)`; runtime cancellation propagation requires device evidence |
| Foreground | No `setForeground` or foreground service was observed in the reviewed worker |
| Notification | No worker-owned foreground notification was observed; notification behavior requires runtime verification if supplied elsewhere |
| Persistence | Queue and progress state are stored through the upload repository/Room-backed model |
| Tags and identity | Unique work name is the upload ID; tags are `tdlib_uploads` and `upload_<id>` |

## Evidence Boundary

This inventory is based on source inspection. It does not claim that process death, screen lock, Doze, OEM battery restrictions, or real Telegram delivery have been tested.
