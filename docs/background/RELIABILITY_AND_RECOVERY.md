# Background Reliability and Recovery

## Current Guarantees from Source

Unique work is keyed by the upload ID. Normal enqueue uses `KEEP`, which avoids replacing existing unique work for the same ID. Explicit retry and resume use `REPLACE`, which intentionally creates a new request for the same logical upload. A completed task is skipped by the worker, reducing repeat execution after a completed state has been persisted.

Room-backed task state records queue status, progress, speed, ETA, timestamps, retry count, source URI, destination ID, and errors. The worker persists progress as engine events arrive and treats a missing terminal completion event as a failure rather than claiming delivery.

Retryable engine errors use WorkManager retry with exponential backoff and a five-attempt boundary based on `runAttemptCount`. Non-retryable errors fail immediately. Network availability is required by the WorkManager constraint, but the engine’s handling of network loss and restoration still requires runtime evidence.

## State Coverage

The source model includes `QUEUED`, `PREPARING`, `UPLOADING`, `RETRYING`, `PAUSED`, `FAILED`, `COMPLETED`, and `CANCELLED` states where used by the domain model. The exact transition behavior for pause/cancel, process recreation, and all UI recovery paths requires runtime tests.

## Unverified Scenarios

The following are `NOT TESTED` in this environment: app backgrounding, screen lock, process termination, device restart, network loss and restoration, battery saver/Doze, OEM restrictions, large-file memory pressure, cancellation at every stage, progress restoration after recreation, and real Telegram delivery after retry.

No claim is made that duplicate uploads are impossible in every scenario. User-initiated duplicate queueing, explicit retry/resume, WorkManager replacement, and Telegram-side delivery semantics require a controlled test matrix before any behavioral change is proposed.
