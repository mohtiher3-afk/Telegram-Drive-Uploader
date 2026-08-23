# Crash Risk Audit

The audit searched the Android source for non-null assertions, unsafe casts, direct indexing, coroutine scopes, and main-thread blocking patterns. Findings are risk candidates, not automatic defects. No production code was changed solely to make tests easier.

| Risk area | Assessment | Action |
|---|---|---|
| `!!` / unsafe casts | Review individually at source locations | No mechanical removal |
| Upload and Telegram callbacks | Must remain lifecycle-aware and fail closed | Covered by existing architecture; add boundary tests before behavior changes |
| WorkManager/Hilt startup | Protected by `check-workmanager-manifest.sh` | CI and device validation required |
| File/URI access | Missing or inaccessible content must surface as recoverable failure | Add controlled storage-failure tests |
| Main-thread blocking | No new blocking code introduced by QA phase | Recheck with lint/profile on device |

The highest-risk unverified areas are runtime startup, authentication, queue recovery, and real TDLib delivery. These require emulator/device evidence.
