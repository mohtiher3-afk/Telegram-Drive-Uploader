# Error Inventory

**Scope:** Existing authentication, Telegram/TDLib, file, network, queue, Worker, scheduler, persistence, and UI paths. No new error hierarchy or recovery feature was added.

| Error source | Location | Current handling | User visible | Recovery | Risk |
|---|---|---|---:|---|---|
| Invalid phone/code/password | `TelegramClientImpl`, auth ViewModel | Validated before TDLib request; mapped to `TelegramError` | Yes | Correct input / retry action | Messages are hard-coded English in the domain model. |
| TDLib error | `TelegramClientImpl` | Maps known codes to rate-limit, session, network, update, or unknown | Yes through auth/engine paths | Re-auth, wait, retry, or stop depending on type | `Unknown` retains raw message and may reach UI. |
| Missing TDLib runtime | Client initialization | `TdLibRuntimeUnavailable` | Yes | Verify APK ABI/native load/configuration | Runtime proof unavailable. |
| Network / server | Client and engine | Codes 420/429/5xx classified retryable; engine exposes retryable flag | Yes through queue state/error | Bounded Worker retry | Code-only classification may be incomplete for other network failures. |
| File metadata/source | Metadata extractor, reader, engine | Exceptions become preparation/engine errors | Yes where caller exposes them | Select another file or retry depending on path | Provider/URI cases require runtime verification. |
| Missing destination | Upload engine preflight | Error result with non-retryable flag | Yes through Worker/queue | Select valid destination | User-facing mapping is not centralized. |
| Unauthenticated account | Upload engine/client | Error result or Telegram auth state | Yes | Login/re-authenticate | Queued task account binding is not persisted. |
| Worker exception | `UploadWorker` | Updates retrying/failed, returns retry/failure based on attempt count | Queue status | Bounded retry or manual retry | Error text and late status races require review. |
| Cancellation | Manager/WorkManager | Cancels unique work and writes cancellation through existing path | Queue action/status | Resume or remove where supported | Cancellation is not a separate engine result in every path. |
| Scheduler/WorkManager enqueue | Upload ViewModel/Manager | Room insertion precedes enqueue; enqueue exceptions are not a common domain error | Usually no immediate user message | Manual inspection/retry | A persisted queued row may outlive failed enqueue. |
| Database/DataStore | Repository/helpers | Caught/logged in selected paths; no destructive reset found | Depends on caller | Retry/reopen | Not all storage failures have user-facing mapping. |
| UI/navigation | Compose/navigation | Material/runtime behavior and route guards | Sometimes | Back/reopen | Full runtime crash coverage unavailable. |

## Privacy Boundary

Diagnostics record category, safe context, operation, and selected error information. The reviewed policy excludes passwords, verification codes, tokens, private file contents, and session secrets. Raw error messages, filenames, URIs, and phone-number-related values require ongoing privacy review before being surfaced or logged.
