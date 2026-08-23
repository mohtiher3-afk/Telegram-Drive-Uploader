# Observability Architecture

## Current Boundary

Observability is local and bounded. `DiagnosticsManager` retains sanitized events in memory, exposes them through a `StateFlow`, prints sanitized summaries to logcat, and supports a user-initiated local export from Settings. There is no authorized remote analytics or automatic crash-reporting pipeline.

## Diagnostic Context

Events may include category, severity, bounded operation/upload identifier, screen, state, sanitized error code, duration, Android API level, app version, timestamp, and safe incident/event identifiers. Phone numbers, codes, passwords, tokens, session data, private messages, private file contents, raw local paths, and raw TdApi objects are prohibited.

## Upload and Worker Diagnostics

The real implementation records creation/preparation, WorkManager enqueue/start/stop, terminal completion/failure, retry, pause/resume/cancel where emitted, and bounded progress-related state. `UploadWorker` intentionally avoids logging every progress update, preserving performance and upload behavior.

## TDLib and Authentication Diagnostics

The application records initialization and safe authentication-error categories. Authorization state handling exists in the Telegram client; the `TELEGRAM_AUTH_STATE` enum is defined but was not confirmed as emitted in the reviewed path. Raw authorization payloads and credentials remain prohibited.

## Database and Startup Diagnostics

Database errors are categorized without dumping rows. Startup and memory-pressure callbacks are represented through the existing diagnostics path; critical memory pressure may clear in-memory events. No artificial startup delay or durable diagnostic database was introduced.

## Crash Handling

No third-party crash-reporting provider was identified. Future provider evaluation requires explicit authorization, privacy review, security review, retention analysis, and release approval.

## Release Behavior

Release logging must remain bounded and sanitized. The current event model contains a hardcoded `appVersion` value of `1.0.0`, while diagnostic export uses `BuildConfig.VERSION_NAME`; this inconsistency is recorded as a maintenance limitation and was not changed in this documentation-only phase.

## Protected Systems

Diagnostics must not change Telegram/TDLib initialization, authentication, queue behavior, WorkManager scheduling, Upload Engine semantics, persistence, retry policy, or user-facing product behavior.
