# Diagnostic Event Catalog

Only events observed in the source or explicitly marked as defined-but-unconfirmed belong in this catalog. No new event is authorized by this document.

| Event | Trigger | Safe data | Sensitive data prohibited | Release level |
|---|---|---|---|---|
| `APP_START` | Application startup | API level, app version, timestamp | Credentials, session state, private data | INFO |
| `TELEGRAM_INIT` | TDLib/client initialization or callback failure | Safe category and sanitized error | Raw TdApi payloads, auth data | INFO / ERROR |
| `TELEGRAM_AUTH_ERROR` | Authentication failure | Safe error category/code | Phone, code, password, session token | ERROR |
| `TELEGRAM_AUTH_STATE` | Enum exists; emission not confirmed in reviewed path | Safe state category only if emitted | Raw authorization payload | INFO |
| `DESTINATION_RESOLUTION` | Destination search/resolution | Safe result category | Chat names, usernames, message text | INFO |
| `UPLOAD_CREATED` | Metadata prepared or queue row inserted | Safe upload ID, byte count where needed | Filename, path, file contents | INFO |
| `UPLOAD_PREPARING` | Worker prepares upload | Safe operation state | File contents and credentials | INFO |
| `WORKER_ENQUEUED` | WorkManager accepts request | Safe upload/worker state | Input payload contents | INFO |
| `WORKER_STARTED` | Worker begins execution | Safe state and operation ID | File contents | INFO |
| `UPLOAD_PROGRESS` | Bounded upload progress state | Safe byte counts/duration where needed | Per-byte payload or excessive frequency | DEBUG / INFO |
| `UPLOAD_RETRY` | Retry is scheduled | Sanitized error category and retry context | Credentials and raw payload | WARN |
| `UPLOAD_PAUSED` / `UPLOAD_RESUMED` | Supported state transition | Safe operation state | Private data | INFO |
| `UPLOAD_CANCELLED` | Cancellation | Safe operation state | Private data | INFO |
| `UPLOAD_COMPLETED` | Successful terminal upload | Safe operation ID and duration | Private content and credentials | INFO |
| `UPLOAD_FAILED` | Terminal failure | Sanitized error category/code and incident ID | Raw exception secrets and paths | ERROR |
| `WORKER_STOPPED` | Worker exits | Safe lifecycle state | Private data | INFO |
| `NETWORK_CHANGED` | High-level network change | Available/unavailable/metered state where available | Headers, cookies, tokens, payloads | INFO |
| `DATABASE_ERROR` | Database/repository failure | Operation category and exception type | Full rows, filenames, user data | ERROR |
| `SETTINGS_CHANGED` | Relevant settings/cache action | Safe setting category | Stored values or credentials | INFO |

The implementation uses bounded in-memory retention of at most 200 events for up to 24 hours. The catalog must be revised if event emission changes, and such a change requires targeted tests and a privacy review.
