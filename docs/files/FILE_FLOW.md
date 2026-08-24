# File Flow

## Actual Lifecycle

```text
Android media selection
 ↓
content URI
 ↓
VideoMetadataExtractor
 ↓
UploadTask / Room-backed queue
 ↓
UploadWorker
 ↓
TelegramUploadEngineImpl
 ↓
streamingFileReader.copyToFile()
 ↓
private temporary staged file
 ↓
TelegramClient.uploadLocalDocument()
 ↓
TDLib / Telegram
 ↓
completion or failure persisted to history
 ↓
staged file deletion in finally
```

The reviewed implementation stores the source URI, display name, MIME type, size, destination ID, progress, dimensions, duration, and state in the upload task. The upload engine copies the URI source to a temporary seekable file because the existing Telegram client handoff consumes a local file path. The copy uses a streaming reader rather than loading the complete source into memory. The staged file is deleted from the engine `finally` block after the handoff flow ends.

## Evidence Boundary

The flow is based on current source inspection. Provider-specific latency, process-death behavior, stale URI recovery, and cleanup after abrupt process termination remain runtime verification items.
