# History, Scheduler, and Notification Test Matrix

**Rule:** No runtime result is reported unless directly observed. `NOT VERIFIED` means the repository or environment does not provide sufficient evidence.

| Scenario | History | Scheduler | Notification | Queue | Status |
|---|---|---|---|---|---|
| Upload complete | Completed Room record appears in history projection. | Not scheduled-specific. | No upload notification exists. | Completed item leaves pending queue. | Repository verified |
| Upload failed | Failed record remains in Room but is excluded from history. | Scheduled failure follows Worker failure path. | No failure notification exists. | Failed filter can show it. | Repository verified |
| Retry | No duplicate history insert; existing task status is changed/re-enqueued. | Retry uses unique-work replacement. | No retry notification action exists. | Task returns through retrying/preparing path. | Repository verified; concurrency unverified |
| Cancel | Cancelled record remains in Room unless removed; history excludes it. | Unique work is cancelled. | No cancel notification exists. | Queue excludes cancelled item. | Repository verified; late callback unverified |
| Schedule | `scheduledAt` is stored on the same upload record. | One-time WorkManager request receives initial delay. | No scheduled notification exists. | Task remains persisted. | Repository verified |
| Schedule cancel | No dedicated history action. | Queue cancel/pause cancels unique work and writes status. | Not applicable. | Queue reflects Room status. | Repository verified; runtime cancellation unverified |
| Schedule execute | Completed task can enter history after confirmed success. | Worker executes after delay under connected-network constraint. | No execution notification exists. | Room status drives queue. | Repository verified; device runtime unverified |
| Restart | Room record remains available. | WorkManager persistence may restore work; no app reconciler found. | No notification restoration path exists. | UI re-reads Room. | Not verified |
| Network loss | History does not change until Worker outcome. | Constraint and retryable errors provide current recovery path. | No progress/failure notification. | State outcome depends on Worker/engine. | Not verified |
| Permission denied | No history-specific permission behavior. | Upload preflight may fail if TDLib unauthorized. | No notification permission dependency. | Error path is existing queue behavior. | Not verified |
| Large history | Full Room Flow is filtered/sorted in memory; no pagination query. | Independent of scheduler. | No notification list. | Potential memory/performance concern. | Repository verified; scale unverified |
| Duplicate completion | Same Room record is updated; no history insertion path exists. | Unique task identity remains task ID. | No notification duplication path. | Race with late callbacks remains unverified. | Conditionally verified |
| Timezone/DST/clock change | History uses local Calendar for TODAY and epoch subtraction for relative periods. | Scheduled timestamp is epoch milliseconds and delay uses current wall clock. | Not applicable. | No special clock recovery found. | Runtime not verified |

## Automated-Test Boundary

No new automated tests were added in this documentation-only review. The matrix identifies future focused tests for history idempotency/deletion, schedule uniqueness/cancellation, worker restart, time changes, and notification mapping if notifications are later implemented. Production code remains unchanged.
