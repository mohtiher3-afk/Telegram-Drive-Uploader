# Transfer Reliability Rules

Uploads are the highest-risk path.

## Truthful state
UI progress, speed, ETA, completion, failure and retry states must come from real persisted/observable transfer state.

Allowed lifecycle states include queued, preparing, uploading, paused, retrying, completed, failed, and cancelled. Do not collapse these into one Boolean.

## Large files
- Stream/chunk files; never load an entire large media file into memory.
- Clean owned staging files on success/cancel/unrecoverable failure.
- Respect URI lifetime and ownership rules already implemented in UploadViewModel.

## Background execution
Preserve the existing WorkManager-based background path and foreground notification/control service.
Do not start long-running transfer work from Composable bodies.

## Errors
Classify retryable vs terminal errors consistently with the existing engine/worker policy.
User-facing errors should explain what failed and what action is available next.

## Concurrency
Keep concurrency bounded. Respect Telegram/API limits and centralized retry/backoff behavior.
