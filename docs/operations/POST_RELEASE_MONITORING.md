# Post-Release Monitoring

Monitoring must use real evidence and must not invent metrics. Review sanitized diagnostics, CI outcomes, and user reports for the following signals after each release:

| Area | Signal | Action threshold |
|---|---|---|
| Crashes | Repeated crash reports grouped by version/device/ABI | Open incident and pause rollout when a reproducible spike is confirmed |
| Authentication | Repeated authorization failures or stuck states | Compare TDLib state and device context; do not request credentials |
| Uploads | Failed, queued, stalled, or falsely completed uploads | Inspect worker, TDLib updates, file access, and retry state |
| Queue | Persistent queue growth or repeated worker failure | Verify constraints, unique work, persistence, and recovery |
| Background execution | Work interrupted after process death or device idle | Run device reproduction and inspect WorkManager evidence |
| Memory | OOM reports or long-upload instability | Reproduce with bounded files and profiler evidence |
| Battery | User reports or measured abnormal wakeups | Compare a controlled device baseline |
| TDLib initialization | Native load, parameter, or authorization initialization failures | Stop rollout until artifact/runtime cause is verified |
| User-reported bugs | Reproducible reports with sanitized context | Classify and route through incident or hotfix procedure |

Record release version, observation window, evidence, owner, decision, and follow-up. Do not interpret absence of reports as proof that a path is correct.
