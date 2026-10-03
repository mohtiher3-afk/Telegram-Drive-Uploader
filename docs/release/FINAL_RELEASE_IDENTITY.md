# Final Release Identity

**Status: CURRENT — phase-28 release-candidate verification.**

| Field | Verified value | Evidence |
|---|---|---|
| Application ID | `com.telegramdrive.uploader` | `app/build.gradle.kts`; release APK manifest inspection |
| Application label, English | `Telegram Drive Uploader` | `app/src/main/res/values/strings.xml`; release APK inspection |
| Application label, Arabic | `محمل تيليجرام درايف` | `app/src/main/res/values-ar/strings.xml`; release resource inspection |
| Version name | `1.0.15` | `app/build.gradle.kts`; release APK manifest inspection |
| Version code | `15` | `app/build.gradle.kts`; release APK manifest inspection |
| Minimum SDK | 24 | `app/build.gradle.kts`; release APK manifest inspection |
| Target SDK | 36 | `app/build.gradle.kts`; release APK manifest inspection |
| Compile SDK | 36 | `app/build.gradle.kts`; release APK manifest inspection |
| Release tag | `v1.0.15` | release documentation and GitHub release record |
| Candidate source commit | `30ac9c902984ca247e2f97e45f95ca890c21e59c` | `docs/release/PRODUCTION_CERTIFICATION.md` |
| Current verification commit | `d50704aab0e08909fe5401e0fa86a3ae750d2084` | Git history; documentation-only cleanup follow-up |

The release identity is internally consistent. The current local verification was performed against the current verification commit and did not alter application identity or behavior.
