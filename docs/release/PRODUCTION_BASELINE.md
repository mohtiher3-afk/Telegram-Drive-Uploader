# Production Baseline

## Application Identity

| Field | Value | Evidence |
|---|---|---|
| Application ID | `com.telegramdrive.uploader` | `app/build.gradle.kts` |
| Product | Telegram Drive Uploader | Repository release metadata |
| Branch | `main` | Git repository state |

## Version

| Field | Value |
|---|---:|
| versionName | `1.0.15` |
| versionCode | `15` |
| Release tag | `v1.0.15` |

## Build Environment

| Component | Version |
|---|---:|
| Java | 17 |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.2.10 |
| compileSdk | 36 |
| targetSdk | 36 |
| minSdk | 24 |
| Android Build Tools | 36.0.0 |
| Android NDK | 26.3.11579264 |

## TDLib

The application uses the checked-in official TDLib Android integration and native artifacts documented by the TDLib artifact checks. The release workflow verified the native artifacts and packaged `libtdjni.so` for each supported ABI. The release process does not mix unknown-source native libraries or placeholder binaries.

## Supported Android Versions

The declared minimum Android API is 24. The target API is 36. Runtime behavior on specific OEM devices remains subject to device validation.

## Supported ABIs

The release provides separate APKs for `arm64-v8a`, `armeabi-v7a`, and `x86_64`. A universal APK is not published by the release workflow.

## Release Commit

The release candidate is represented by commit `30ac9c902984ca247e2f97e45f95ca890c21e59c`, tagged `v1.0.15`. Documentation was subsequently updated on `main` in commit `1a938d9`; those documentation changes do not alter the published binaries.

## Known Limitations

The v1.0.15 artifacts were built, signed, verified, and published by GitHub Actions. Device-level evidence for real Telegram authentication, session restoration, real upload, background recovery, battery behavior, and visual RTL/accessibility checks remains incomplete. These limitations are recorded in `KNOWN_LIMITATIONS.md` and the production handoff report.
