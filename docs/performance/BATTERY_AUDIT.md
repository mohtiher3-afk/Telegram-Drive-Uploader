# Battery Audit

Potential battery-sensitive areas are WorkManager retries, progress persistence, notification updates, TDLib/network activity, and any long-lived Flow collectors. The current architecture retains background execution and retry behavior because correctness and recoverability take priority.

No polling loop, `GlobalScope`, artificial delay, or speculative notification throttling was introduced or removed. Battery impact is **NOT MEASURED — TOOLING UNAVAILABLE** and requires a physical-device test with Android Battery Historian or equivalent profiling.
