# AGENTS.md — Permanent Rules

- DEFINITION OF DONE: a task is only "done" when there is a working, runtime-verified change. Producing a Markdown report, audit, or "inventory" document does NOT count as done unless a document was explicitly the requested deliverable.
- EVIDENCE OVER CLAIMS: never write "verified" or "confirmed" without attaching the actual artifact (log output, test result, CI run link, screenshot). No artifact = write "not yet verified".
- ONE VERIFIED CHANGE AT A TIME: finish, test, and merge one fix before starting the next. Never accumulate multiple unverified phases before running an end-to-end check.
- REPRODUCE BEFORE YOU FIX: for any bug, first reproduce the failure with a log/screenshot/failing test. Only then write the fix.
- TEST THE FIX, NOT JUST THE BUILD: "CI is green" is not equivalent to "the feature works". Anything touching TDLib, WorkManager, upload state, or authentication requires a runtime check on a device/emulator before being marked resolved.
- NO SILENT SCOPE CREEP: do not touch files or architecture outside the explicit scope of the current phase file.
- SMALLEST POSSIBLE DIFF: prefer the minimal change that fixes the confirmed root cause over a broad refactor.
- MANDATORY REGRESSION TEST PER FIX: every confirmed bug fix ships with a test that fails on the old code and passes on the new code.
- CI IS A GATE, NOT A FORMALITY: nothing is merged to main with a red or skipped CI check.
- NO FAKE OR SIMULATED SUCCESS PATHS: never let a UI or log report "success" or "uploaded" based on local/staged state alone. Success must be gated on a confirmed signal from the real system.
- SECRETS AND ARTIFACTS DISCIPLINE: never commit keystores, API keys, .env values, session data, or built APK/AAB files.
- HONEST LIMITATION REPORTING: if something cannot be fully verified (missing device, credentials, environment), say so explicitly. Never substitute documentation for real verification.
- STOP-AND-ASK TRIGGERS: stop and ask the user before (a) starting a new architecture-wide refactor, (b) publishing a signed release, (c) any action that could overwrite another engineer's in-progress work, (d) reducing test coverage to unblock a build.