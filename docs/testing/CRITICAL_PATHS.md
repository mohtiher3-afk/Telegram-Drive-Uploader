# Critical Paths

| Path | Required evidence |
|---|---|
| Application start | Startup and navigation render without crash |
| Authentication | TDLib authorization states, reconnect, logout, and session restoration |
| Telegram initialization | Native load, `Client.create()`, TDLib parameters, and fail-closed behavior |
| File selection | Android picker result, URI access, metadata, and readable stream |
| Upload creation | Valid task persistence and unique work enqueue |
| Upload queue | Ordering, persistence, duplicate protection, and truthful status |
| Upload execution | Real TDLib upload start, updates, completion, and failure |
| Progress | Byte-based progress, speed/ETA presentation, and no false completion |
| Pause | Request and state transition |
| Resume | Request and state transition after pause or interruption |
| Cancel | Cancellation request and terminal state |
| Retry | Backoff, retry limit, and error visibility |
| Background execution | WorkManager constraints, process death, recovery, and notification |
| Persistence | Room/DataStore read/write and data preservation |
| History | Completed/failed task display and filtering |

The current release has source and CI evidence for several build-time gates, but real Telegram authentication/upload, process-death recovery, and broad device UI execution remain `NOT VERIFIED` in the production handoff.
