# Privacy Logging Policy

## What May Be Logged

The application may record sanitized lifecycle events, timestamps, app/API context, safe operation or upload identifiers, bounded state, safe error categories, retry counts where available, and measured durations where useful for diagnosis.

## What Must Never Be Logged

Passwords, Telegram authentication codes, API hashes, bot tokens, session tokens, raw Telegram credentials, private messages, private file contents, full local paths, unmasked filenames, cookies, authorization headers, and unnecessary personal information are prohibited.

## Redaction

All diagnostic text and user-controlled identifiers must pass the existing sanitization path before in-memory retention, logcat output, display, or export. Demonstrations and reports must use placeholders such as `[REDACTED_PHONE]`, `[REDACTED_CODE]`, or `[REDACTED_CREDENTIAL]`; actual secret values must never appear in examples.

## Export and Support

Diagnostic export is user-initiated and local. Support requests should ask only for app version, Android/API version, device/ABI, network condition at a high level, operation, visible error, reproduction steps, timestamps, and incident IDs. Support must never request credentials, session files, private media, or unredacted logs.

## Release Behavior

Release builds must avoid excessive DEBUG/VERBOSE output and must not add external analytics or crash reporting without explicit authorization and privacy review. Diagnostics must not alter application, TDLib, authentication, upload, queue, WorkManager, or persistence behavior.

## Retention

The current implementation retains sanitized events in memory with bounded size and age. No durable remote retention policy exists. Any future remote collection requires an explicit requirement, data-minimization review, access control, retention limit, user disclosure, and release approval.
