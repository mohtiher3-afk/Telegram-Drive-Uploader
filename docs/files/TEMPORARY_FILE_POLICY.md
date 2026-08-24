# Temporary File Policy

## Current Behavior

`TelegramUploadEngineImpl` creates a temporary file with the `tdlib-upload-` prefix and a sanitized, truncated suffix derived from the task filename. `StreamingFileReader.copyToFile` copies the content URI into that file using streaming I/O. The engine passes the absolute path to the existing Telegram client and calls `stagedFile.delete()` from `finally` after the upload flow ends.

## Ownership and Lifetime

The upload engine owns the staged file for one upload attempt. The file is intended to exist only for the staging and TDLib handoff interval. It is not stored in Room and is not presented as a durable user file.

## Failure and Cancellation

The `finally` block attempts deletion after success, engine error, or thrown exception. The repository does not establish a separate startup scavenger for orphaned staging files, and deletion success is not surfaced as a user-visible state. Abrupt process termination before `finally` runs may leave a stale file; cleanup after process death and app restart is not verified.

## Safety Rules

Staged files must not be deleted while an active upload still references them. Future cleanup must identify only files owned by this uploader, avoid active work, and be validated against process death, cancellation, failure, restart, and concurrent uploads. No cleanup redesign is part of this documentation phase.
