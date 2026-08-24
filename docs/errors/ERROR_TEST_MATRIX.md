# Error Test Matrix

**Rule:** Do not claim that every possible error is handled. Use `PASS` only for observed tests and `NOT VERIFIED` for unavailable runtime evidence.

| Error | Layer | User message | Action | Retry | Logged | Status |
|---|---|---|---|---|---|---|
| Network/server | TDLib/client/engine | Network-unavailable or engine error path | Retry/wait | Yes for classified 420/429/5xx under limit | Yes, safe context | Repository verified; runtime not verified |
| File/URI | Reader/engine/ViewModel | Existing exception/error path | Select another file or retry where exposed | Usually no unless engine marks retryable | Yes where caught | Runtime/provider behavior not verified |
| TDLib | Client/engine | Mapped known error or raw unknown message | Re-auth/retry/stop by category | Category-dependent | Yes | Mapping verified; privacy/localization risk |
| Authentication | Auth client/ViewModel | Invalid phone/code/password/session | Correct input or login again | No blind retry | Yes without secrets | Repository verified; runtime not verified |
| Destination | Engine/preflight | Destination required/unavailable | Select valid destination | No silent reroute | Yes where applicable | Repository verified; runtime not verified |
| Upload | Engine/Worker | Engine error string and queue state | Retry or manual action | Retryable only | Yes | Repository verified; runtime not verified |
| Worker | Worker | Queue status and diagnostic path | Bounded retry/manual retry | Max-attempt policy | Yes | Repository verified; races unverified |
| Database | Repository/DAO | Caller-dependent error state | Reopen/retry where possible | Not globally defined | Selected paths | Partial; no destructive fallback found |
| Scheduler | ViewModel/Manager | No centralized scheduler error model found | Inspect/retry existing task | Not globally defined | Enqueue diagnostics | Failure window documented |
| Cancellation | Manager/Worker | Cancelled queue state | Resume/remove where supported | No automatic retry | Existing diagnostic path | Runtime callback race unverified |

## Sensitive Data

No passwords, verification codes, session secrets, tokens, or private file contents should enter user messages or diagnostics. The current `TelegramError.Unknown` raw message path is a risk requiring a separately approved mapping fix.
