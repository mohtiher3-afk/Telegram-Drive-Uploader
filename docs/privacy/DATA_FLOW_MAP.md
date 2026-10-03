# Data Flow Map

## Upload Flow

```text
User selects media
        ↓
Android picker / content resolver
        ↓
Application reads required metadata and bytes
        ↓
Room stores upload metadata, state, and progress
        ↓
WorkManager carries the safe upload ID and runs the worker
        ↓
TDLib authenticates and uploads the media to the selected Telegram destination
        ↓
Telegram infrastructure receives the upload and resulting message/file data
```

The application does not identify a separate third-party upload service in the reviewed source. File contents are not sent to the diagnostics system.

## Authentication Flow

```text
User provides Telegram authentication input
        ↓
TDLib authorization flow
        ↓
Telegram infrastructure
        ↓
TDLib-managed session state
        ↓
Application receives safe authorization state and display data as needed
```

Authentication secrets and session internals are not part of diagnostics or support exports. The exact internal TDLib storage behavior is outside the application’s documented persistence contract.

## Local Persistence Flow

```text
Application settings ───────→ DataStore
Upload metadata/state ───────→ Room database
Work scheduling ─────────────→ WorkManager
Transient media processing ──→ memory/cache/files as required by the upload path
Diagnostics ─────────────────→ bounded in-memory StateFlow + local logcat/export
```

The repository’s Android backup configuration excludes the Room database and `datastore/` from cloud backup and device transfer. No undocumented database-copying or remote diagnostic storage mechanism is introduced by this review.

## Diagnostic and Support Flow

```text
Application events
        ↓
DiagnosticsManager sanitization
        ↓
Bounded local memory / logcat
        ↓
User reviews optional local export
        ↓
Support receives only redacted evidence when voluntarily shared
```

Diagnostics may include safe operation identifiers, state, categories, timestamps, API level, app version, and sanitized errors. Credentials, codes, passwords, tokens, private messages, private file contents, and raw Telegram payloads are prohibited.

## External Destinations

The confirmed external data destination for core functionality is Telegram through TDLib. No third-party analytics, advertising, remote logging, or crash-reporting data service was identified in the reviewed application code or dependency configuration.
