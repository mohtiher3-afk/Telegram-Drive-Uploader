# Incident Response

All incidents begin with a safe report containing app version, Android version, device/ABI, network condition, operation, visible error, reproduction steps, and sanitized diagnostics. Never request Telegram credentials, login codes, passwords, session files, or signing material.

| Incident | Detect | Contain | Investigate | Fix | Verify | Release |
|---|---|---|---|---|---|---|
| Crash spike | User reports and sanitized diagnostics | Identify affected version and pause rollout | Reproduce from stack trace and device context | Minimal scoped fix | Unit, lint, build, and device regression | Manual signed release after review |
| Authentication failure | Authorization state or user report | Avoid retry loops and preserve fail-closed behavior | Inspect TDLib state mapping and sanitized logs | Correct only confirmed state/compatibility defect | Real login and reconnect test | Release notes and monitor |
| Upload failure | Upload status/error event | Keep truthful failure state; do not claim success | Inspect queue, worker, TDLib updates, and file access | Minimal upload-path fix | Real upload, retry, cancel, and restart tests | Manual release after gates |
| TDLib failure | Native/artifact checker or runtime error | Stop TDLib activation; preserve fail-closed behavior | Check version, ABI, ELF, JNI load, and parameters | Rebuild matching official artifacts | Artifact and device validation | Do not publish until verified |
| Data corruption | Room/DataStore error or user report | Preserve files and stop destructive migration actions | Inspect schema/version and backups | Add analyzed migration or repair | Upgrade/data-preservation test | Release only with migration evidence |
| Security incident | Secret scan, report, or suspicious log | Revoke/rotate exposed secret and restrict access | Determine scope without copying sensitive values | Remove exposure and patch root cause | Secret scan and security review | Security-approved release |
| Performance regression | Measured before/after regression | Disable only the affected optional path if safe | Compare profiler traces and upload metrics | Revert or narrowly optimize measured path | Repeat baseline and stress tests | Release with evidence |

All containment actions must preserve user data. Database rollback is never a first response; migration analysis is required before any schema action.
