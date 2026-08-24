# Invalid Upload-State Transitions

## Review Rule

This document records what the current implementation can permit or what is not supported. It does not add a transition validator and does not change production semantics.

| Transition / contradiction | Current evidence | Decision |
|---|---|---|
| `COMPLETED → QUEUED` | No normal caller requeues completed work; Worker exits successfully when it reloads a completed task. | Not observed in normal path. |
| `CANCELLED → COMPLETED` | Cancellation writes Room and cancels unique work, but a late engine/Worker callback could still write progress or completion if the operation was already in flight. | Race risk; not runtime verified. |
| `FAILED → COMPLETED` | Manual retry writes `RETRYING`; completion is only emitted by a later Worker. A stale prior Worker could theoretically write after a retry if cancellation is not fully settled. | Race risk; not runtime verified. |
| `PAUSED → UPLOADING` | `updateProgress()` always sets status to `UPLOADING`; a progress callback after pause can overwrite `PAUSED`. | Verified code-level race risk. |
| `UPLOADING → PAUSED` while WorkManager still runs | Queue ViewModel writes `PAUSED` before `cancelUniqueWork`; cancellation is asynchronous. | Temporary inconsistency is possible until cancellation settles. |
| `FAILED + ACTIVE` | Queue treats `FAILED` as non-active, but a Worker may still be executing while status is changed by a UI retry/cancel action. | Runtime race not verified. |
| `UPLOADING + COMPLETED` | A completion writer follows terminal success; a late progress writer could update status back to `UPLOADING` after completion. | Possible from writer ordering; no race test found. |
| Unknown persisted status | Room string is mapped through repository conversion; handling of an obsolete/invalid enum value was not found. | Unknown-state behavior is not verified; no silent repair added. |

## Completion Safety

The Worker does not mark an upload completed merely because its method reaches the end. It requires `UploadEngineResult.Success`, and the engine emits success only after `TelegramUploadEvent.Completed`. A stream ending without a terminal event is written as failed. This protects the primary completion path, while concurrent status writers remain a separate risk.

## Decision

No automatic transition repair, timeout, schema change, or queue reset was justified under the supplied controlled-maintenance protocol. Reproduction on a device and focused concurrency tests are required before changing any of these boundaries.
