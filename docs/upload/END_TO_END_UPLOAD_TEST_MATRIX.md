# End-to-End Upload Test Matrix

**Rule:** Static repository evidence is not runtime proof. Use `PASS` only for an observed check; use `NOT VERIFIED` when a device, emulator, account, or test fixture is unavailable.

| Scenario | Auth | Destination | File | Queue | Worker | TDLib | History | Notification | Result |
|---|---|---|---|---|---|---|---|---|---|
| Normal upload | Existing TDLib session required | Stable chat ID captured | Same persisted URI staged | Room insert then unique work | Reloads by `upload_id` | Send-success event required | Completed projection | Not implemented | Conditionally verified statically |
| Failure | Auth/TDLib error path exists | Destination failure maps to engine/Worker result | Source/preflight failure maps to failure | Failed or retrying status | Result determines retry | No confirmed success required | Failed excluded | Not implemented | Repository verified; runtime not verified |
| Retry | Current session reused | Task destination retained | Task URI retained | Existing task re-enqueued with replace | Retry ceiling exists | New engine attempt | No separate history insert | Not implemented | Repository verified; race unverified |
| Cancel | Session unaffected by UI cancel | ID retained in task | Worker cancellation may race with callbacks | Unique work cancelled and status written | Late callback behavior unverified | Abort/TDLib outcome unverified | Cancelled excluded | Not implemented | Not verified runtime |
| Background | Session may be required | Persisted ID | Persisted URI | WorkManager owns request | Constraint-backed execution | Real delivery unverified | Room projection | No foreground notification | Not verified |
| Restart | TDLib session restoration required | Task ID persisted | URI availability unverified | WorkManager/Room persistence present | No explicit reconciler found | Runtime unverified | Re-reads Room | No notification restoration | Not verified |
| Process death | Session continuity unverified | Captured ID | Provider permission/unavailability unverified | Task persists in Room | Worker recreation input is ID only | Runtime unverified | Room record remains basis | Not implemented | Not verified |
| Network loss | Session may remain authorized | Destination unchanged | File staging behavior depends on timing | WorkManager constraint/retry path | Retryable result exists | Recovery not assumed | No history until completion | Not implemented | Not verified |
| Scheduled | Session required at execution | Captured before scheduling | URI captured before scheduling | `scheduledAt` persisted | Initial delay passed | Runtime delivery unverified | Completion would project | Not implemented | Repository verified; runtime not verified |
| Multiple uploads | One session assumption | Each task captures ID | Each task captures URI | One unique work name per task | Concurrent behavior unverified | TDLib serialization/runtime unverified | One record per task | Not implemented | Not verified |
| Progress 0/1/50/99/100 | Session not relevant | Task identity retained | Task identity retained | Room progress writer exists | Late progress race known | Engine progress path exists | History ignores progress | Not implemented | Boundary conversion unit-tested; runtime not verified |
| Terminal progress | Session not relevant | ID retained | URI retained | Terminal status writer exists | Late writes may race | Engine result boundary exists | Completion projection exists | Not implemented | Conditionally verified |

## Automated Tests

The repository has focused tests for upload completion policy, WorkManager policy, media formats, Telegram message content, and telemetry formatting. The end-to-end implementation audit did not add a fake Telegram integration or production mock. Additional tests for transaction orchestration, failure windows, and cancellation races require explicit seams or a separately approved repair phase.

## Real Telegram Test

No controlled real Telegram upload was executed in this review. Therefore no duration, destination confirmation, progress trace, history confirmation, or notification result is claimed.
