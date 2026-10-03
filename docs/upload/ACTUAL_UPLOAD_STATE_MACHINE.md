# Actual Upload State Machine

## States Actually Present

| State | Entry condition | Exit conditions | Allowed transitions observed | Owner | Persistence | UI / Worker representation |
|---|---|---|---|---|---|---|
| `QUEUED` | Upload task is inserted and WorkManager work is enqueued, or resume explicitly writes it. | Worker starts; user pauses/cancels; retry/resume scheduling. | `QUEUED → PREPARING`, `QUEUED → PAUSED`, `QUEUED → CANCELLED`, `QUEUED → RETRYING` through current callers. | Upload ViewModel / queue actions / Room | Room; active-query state | Active queue item; Worker may start from this state. |
| `PREPARING` | Worker loads a task and begins preflight/source staging. | Progress reaches engine, terminal error, exception, or cancellation. | `PREPARING → UPLOADING` through `updateProgress`; `PREPARING → FAILED` on unconfirmed/error paths; pause/cancel can be requested by UI. | Worker and Room | Room | Progress indicator is rendered for preparing; Worker performs engine handoff. |
| `UPLOADING` | DAO progress update or active upload path. | Confirmed success, failure, retry, cancellation, or pause request. | `UPLOADING → COMPLETED`, `FAILED`, `RETRYING`, `PAUSED`, or `CANCELLED` through current writers. | Worker / DAO / queue actions | Room | Progress bar, speed, ETA, pause/cancel controls. |
| `PAUSED` | Queue ViewModel writes paused and cancels unique WorkManager work. | Resume writes `QUEUED` and re-enqueues. | `PAUSED → QUEUED`; a late progress write could overwrite it to `UPLOADING`. | Queue ViewModel / Room / WorkManager | Room plus cancelled work state | Paused filter and resume action. |
| `RETRYING` | Worker classifies a retryable failure, or user requests retry. | WorkManager retry/replacement starts or task reaches another writer. | `RETRYING → PREPARING` on worker execution; can be overwritten by progress/completion/failure. | Worker / queue actions | Room plus WorkManager attempt count | Active queue filter and pause/cancel/retry semantics. |
| `COMPLETED` | Worker receives `UploadEngineResult.Success`, which requires engine terminal completion event. | UI deletion/clear-history only; no upload transition out is supported. | No normal re-upload transition observed. | Worker / Room | Room | Excluded from queue; included in history. |
| `FAILED` | Terminal non-retryable error, exhausted retry, unconfirmed stream, missing task, or exception without further retry. | User retry/retry-all. | `FAILED → RETRYING`; retry path re-enqueues. | Worker / queue actions / Room | Room | Failed filter and retry action; excluded from history. |
| `CANCELLED` | Queue ViewModel writes cancelled and cancels unique WorkManager work. | Record deletion; no resume-from-cancel path observed. | No normal transition out of cancelled. | Queue ViewModel / Room / WorkManager | Room plus cancelled work state | Excluded from queue and history; removable. |

## Lifecycle Paths

The normal intended path is:

```text
CREATED / inserted
  → QUEUED
  → PREPARING
  → UPLOADING
  → COMPLETED
```

The implementation also supports failure and retry paths:

```text
QUEUED/PREPARING/UPLOADING
  → FAILED
  → RETRYING
  → PREPARING
```

Pause and cancellation are implemented through Room status writes followed by WorkManager cancellation:

```text
QUEUED/PREPARING/UPLOADING/RETRYING → PAUSED → QUEUED
QUEUED/PREPARING/UPLOADING/RETRYING → CANCELLED
```

There is no separate `CREATED` enum state. Insertion creates the task with its current status, and the reviewed creation path uses `QUEUED` before enqueue.

## Valid Transition Matrix

| From | To | Allowed in current code | Trigger |
|---|---|---|---|
| `QUEUED` | `PREPARING` | Yes | Worker starts. |
| `PREPARING` | `UPLOADING` | Yes | DAO progress update forces uploading. |
| `UPLOADING` | `COMPLETED` | Yes | Confirmed engine success. |
| `UPLOADING` | `FAILED` | Yes | Terminal error, unconfirmed stream, or exception. |
| `UPLOADING` | `RETRYING` | Yes | Retryable error within attempt limit. |
| `FAILED` | `RETRYING` | Yes | Manual retry or retry-all. |
| `PAUSED` | `QUEUED` | Yes | Resume action. |
| Active state | `PAUSED` | Yes | Pause action writes Room then cancels unique work. |
| Active state | `CANCELLED` | Yes | Cancel action writes Room then cancels unique work. |
| `RETRYING` | `PREPARING` | Yes | Replacement/retry Worker starts. |
| `COMPLETED` | active state | No normal path found | No re-upload transition observed. |
| `CANCELLED` | active state | No normal path found | No resume-from-cancel path observed. |

## Important Consistency Boundary

The state machine is not centrally enforced. `UploadDao.updateStatus()` accepts any enum string, and `updateProgress()` unconditionally sets `UPLOADING`. Therefore the table describes transitions permitted by current call paths, not a formally validated transition algebra. The verified pause/cancel versus late-progress race is documented separately and was not changed in this phase.
