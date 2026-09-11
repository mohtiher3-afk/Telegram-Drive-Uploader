# Temporary File Policy

## Current Behavior

`TelegramUploadEngineImpl` creates a temporary file with the `tdlib-upload-` prefix and a sanitized, truncated suffix derived from the task filename. `StreamingFileReader.copyToFile` copies the content URI into that file using streaming I/O. The engine passes the absolute path to the existing Telegram client and calls `stagedFile.delete()` from `finally` after the upload flow ends.

## Ownership and Lifetime

The upload engine owns the staged file for one upload attempt. The file is intended to exist only for the staging and TDLib handoff interval. It is not stored in Room and is not presented as a durable user file.

## Failure and Cancellation

The `finally` block attempts deletion after success, engine error, or thrown exception. The repository does not establish a separate startup scavenger for orphaned staging files, and deletion success is not surfaced as a user-visible state. Abrupt process termination before `finally` runs may leave a stale file; cleanup after process death and app restart is not verified.

## Durable Staging and Compression Files

In addition to the one-shot engine copy, the ViewModel creates durable staging and compressed scratch files under `context.filesDir`:

- **Staging dir**: `filesDir/staged-uploads/<task-id>-<sanitized-filename>` — created by `snapshotContentToOwnedFile` when a content:// URI is picked. The `UploadTask.sourceUri` is rewritten to this `file://` path so the worker reads a stable local file without depending on the original content:// grant surviving OS cache eviction.
- **Compression dir**: `filesDir/compressed/<uuid>.mp4` — written by `VideoCompressor` when a quality preset other than ORIGINAL is selected.

Because these files live in durable `filesDir` rather than the evictable `cacheDir`, they are NOT removed by OS cache pressure and must be explicitly deleted.

### Ownership and Cleanup

`OwnedStagedFileStore` centralizes ownership rules and deletion:

- **Owned paths**: only files under `filesDir/staged-uploads/` and `filesDir/compressed/` (verified by absolute path prefix).
- **Worker terminal cleanup**: `UploadWorker` calls `deleteOwnedFilesFor(task)` on COMPLETED and CANCELLED paths only. FAILED and PAUSED preserve the file so retry and resume can reuse it.
- **VM cleanup on cancel/remove**: `QueueViewModel.cancelUpload` and `removeUpload` also delete owned files — belt-and-suspenders for the case where WorkManager cancels an enqueued worker before `doWork` runs.
- **Compression restore cleanup**: switching the preset back to ORIGINAL deletes the now-orphaned compressed output before restoring the pre-compression sourceUri.

### Safety

`deleteOwnedFile` and `deleteOwnedFilesFor` are guarded to only delete `file://` URIs whose resolved absolute path falls inside the owned directories. Arbitrary content://, http://, or unrelated `file://` paths are never touched.
