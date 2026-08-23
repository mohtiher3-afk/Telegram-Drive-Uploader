# Release Checklist

| Check | Status | Evidence / Note |
|---|---|---|
| Final audit | PASS | Final audit reports and handoff documents exist |
| Release blockers | FAIL | Runtime/device evidence remains outstanding |
| Version | PASS | `1.0.15` / versionCode `15` |
| Application ID | PASS | `com.telegramdrive.uploader` |
| Manifest | PASS | Static manifest review and guards |
| Permissions | PASS | Permission audit completed |
| TDLib artifacts | PASS | Release workflow artifact gate passed for all supported ABIs |
| R8/resource shrinking | PASS | Release configuration reviewed and build completed |
| Release build | PASS | Signed multi-ABI workflow `32630539974` |
| AAB | NOT APPLICABLE | Current release workflow publishes APKs only |
| APK | PASS | Three signed ABI APKs published |
| Signing | PASS | CI signature verification passed; values remain secret |
| Authentication smoke test | NOT VERIFIED | No real-device session evidence in this handoff |
| Real upload smoke test | NOT VERIFIED | No real-device Telegram delivery evidence |
| Background test | NOT VERIFIED | Process-death and device-idle test pending |
| Notification test | NOT VERIFIED | Device validation pending |
| Security static check | PASS | Redacted security gate passed |
| CI final check | PASS | Release workflow completed successfully |
| Git cleanliness | PASS | Final documentation commit is clean after push |
| Documentation | PASS | Release and operations indexes and procedures exist |

## Release Status

**NOT CERTIFIED** for unrestricted production handoff. The signed v1.0.15 APKs are published, but authentication, real upload, background recovery, notification, and runtime UI evidence remain `NOT VERIFIED`. Do not claim every user flow is production-proven.
