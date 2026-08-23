# Production Logging Policy

## Production Logs

Production diagnostics may record lifecycle events, upload state transitions, bounded progress metadata, authorization state categories, and sanitized error categories. Events use stable categories and incident IDs for errors. In-memory retention is bounded and time-limited by `DiagnosticsManager`.

## Debug Logs

Debug logging is allowed only during local diagnosis and must use the same sanitization path. Temporary verbose logging must be removed or disabled before a release commit.

## Sensitive Data Policy

Never log API hashes, bot tokens, passwords, authorization codes, phone numbers, session data, private keys, full local paths, or unmasked filenames. Diagnostic sanitization must be preserved when adding new event fields. Logs must not be used as a substitute for secure secret storage.

## Telegram Session Information

Record only authorization state categories and safe failure codes. Do not record phone numbers, login codes, passwords, session database contents, or raw TDLib payloads.

## Upload Information

Record upload ID, safe status, byte counts when necessary, and sanitized error categories. Do not log raw file paths, unmasked filenames, file contents, credentials, or authorization headers.

## Error Reporting

Exported diagnostics should contain the app version, device/API context supplied by the user, event timestamps, categories, and sanitized messages. An incident ID may be shared with maintainers. Users must not be asked to send Telegram credentials or session files.
