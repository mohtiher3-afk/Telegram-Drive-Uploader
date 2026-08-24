# Background Execution and Upload Reliability

This directory documents the existing WorkManager-based upload path and its evidence boundaries. It does not authorize changes to upload behavior.

| Document | Purpose |
|---|---|
| `BACKGROUND_ARCHITECTURE.md` | Actual UI-to-TDLib background flow. |
| `WORKER_INVENTORY.md` | Worker input, output, constraints, retries, cancellation, and persistence. |
| `RELIABILITY_AND_RECOVERY.md` | Unique work, duplicate-risk analysis, states, and recovery limits. |
| `FINAL_BACKGROUND_REPORT.md` | Final review status and unverified runtime scenarios. |

Any future change affecting WorkManager, workers, queue persistence, retries, cancellation, notifications, process death, background execution, or upload reliability must use this protocol and include runtime evidence where practical.
