# Error Flow Architecture

## Authentication and TDLib

`TDLib/validation failure → TelegramClientImpl mapping → TelegramError → TelegramAuthViewModel state/message → TelegramAuthScreen`

Known errors include invalid phone, invalid code, invalid password, rate limiting, network unavailability, expired session, invalid API credentials, required app update, and unavailable TDLib runtime. Each Telegram error now maps to a localized Android resource. Unknown errors map to a safe generic resource instead of exposing the stored raw TDLib message to the user.

## Upload

`source/destination/session preflight or TDLib event → TelegramUploadEngineResult.Error(message, isRetryable) → UploadWorker classification → Room status (RETRYING or FAILED) → Queue UI`

The Worker returns `Result.retry()` only for retryable engine errors while below the configured attempt ceiling. Otherwise it returns `Result.failure()`. A successful Worker result is written only after the engine reports actual Telegram send success.

## File and Storage

`ContentResolver/metadata/stream failure → engine or ViewModel exception path → visible preparation/error state or Worker failure`. The current code uses exception messages in selected technical paths. No destructive database recreation or silent file substitution was found.

## User-Facing Message Model

The repository has a Telegram-specific sealed error model and an upload engine error containing a string plus retryability. It does not have one common localized error presentation model with a title, description, and typed action for every category. `TelegramError.getLocalizedMessage()` contains hard-coded English, and `Unknown` returns its stored raw message. These are documented as risks, not silently redesigned in this phase.

## Recovery Actions

Actual recovery actions include entering corrected authentication data, waiting after rate limiting, re-authentication for expired session, retrying failed queue items, pausing/cancelling/resuming work through WorkManager, selecting a valid destination, and selecting/ retrying a file. Actions that are not implemented, such as notification actions or automatic orphan reconciliation, are not promised.

## Separation of Diagnostics and UI

Developer diagnostics may retain safe category/context/error information. User-facing surfaces should use localized, actionable messages and must not expose stack traces, credentials, verification codes, passwords, tokens, or private file content. The current raw-message path requires a separately approved localization/privacy fix if it is to be changed.
