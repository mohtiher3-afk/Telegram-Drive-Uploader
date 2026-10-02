# Controlled Bug Documentation Template

Copy this directory to `docs/bugs/<bug-id>/` after a bug is reported and before source changes begin. Replace placeholders with redacted evidence. Do not include passwords, Telegram codes, tokens, API secrets, private user data, or unredacted sensitive logs.

| Document | Required purpose |
|---|---|
| `BUG_REPORT.md` | Reproduction context, expected/actual behavior, frequency, severity, and safe evidence. |
| `ROOT_CAUSE.md` | Confirmed invalid state, why it was possible, and affected components. |
| `HIGH_RISK_PLAN.md` | Required before implementation for TDLib, JNI, ABI, authentication, upload, WorkManager, database, security, or native-library bugs. |
| `BUG_FIX_REPORT.md` | Fix, regression test, validation, risk, and release impact. |

A bug is not `FIXED` unless the root cause is confirmed, the fix is implemented, the regression test passes where practical, and build validation passes. Otherwise use `PARTIALLY FIXED` or `UNVERIFIED`.
