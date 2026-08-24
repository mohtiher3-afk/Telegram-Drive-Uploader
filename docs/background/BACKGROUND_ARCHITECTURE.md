# Background Upload Architecture

## Current Flow

```text
UI
 ↓
Upload Queue / Room-backed UploadRepository
 ↓
UploadManagerImpl
 ↓
WorkManager unique work
 ↓
UploadWorker (CoroutineWorker)
 ↓
TelegramUploadEngine
 ↓
Telegram / TDLib
```

The UI creates or observes `UploadTask` records through the upload repository and manager. `UploadManagerImpl` creates a one-time `UploadWorker` request with the `upload_id` input, a connected-network constraint, exponential backoff beginning at 30 seconds, and tags `tdlib_uploads` and `upload_<id>`. Work is enqueued as unique work using the upload ID as the unique name.

`UploadWorker` reloads the task from the repository, skips already-completed tasks, moves active work to `PREPARING`, consumes progress from `TelegramUploadEngine`, persists byte and timing fields, and records terminal completion or failure only after the engine emits a terminal result. A stream ending without a terminal result is treated as unconfirmed delivery and fails the task.

No foreground service was observed in the reviewed source. Background execution is provided through WorkManager and the existing application constraints. The application does not use `GlobalScope` in the reviewed upload path.

## Runtime Evidence Boundary

Static source and build evidence confirm the architecture above. Process-death, screen-lock, battery-restriction, OEM, and device-level runtime behavior remain untested in the current environment. No claim of uninterrupted background execution is made.
