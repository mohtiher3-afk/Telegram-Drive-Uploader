# Release Certification

**Status: CURRENT — phase-28 final release-candidate gate.**

## Release Candidate

Telegram Drive Uploader `v1.0.15`, release tag `v1.0.15`, using official TDLib v1.8.66 artifacts for `arm64-v8a`, `armeabi-v7a`, and `x86_64`.

## Exact Commit

The release candidate source commit recorded by the existing release certification is `30ac9c902984ca247e2f97e45f95ca890c21e59c`. The current verification tree is `d50704aab0e08909fe5401e0fa86a3ae750d2084`, containing documentation-only cleanup changes and no application behavior changes.

## Version

| Field | Value |
|---|---|
| Application ID | `com.telegramdrive.uploader` |
| Version name | `1.0.15` |
| Version code | `15` |
| Minimum SDK | 24 |
| Compile/target SDK | 36 |
| English label | Telegram Drive Uploader |
| Arabic label | محمل تيليجرام درايف |

## Build Environment

The clean verification used JDK 17, Android SDK API 36, Android Gradle Plugin 8.7.3, Kotlin 2.2.10, Gradle Wrapper 8.9, Android NDK 26.3.11579264 for the existing native artifact workflow, and the repository’s configured Gradle settings. No versions or dependencies were changed during certification.

## Verification Summary

Clean, compile, JVM test, lint, debug APK, release APK, and release AAB tasks all completed successfully. TDLib artifact validation, repository security scanning, resource integrity, WorkManager manifest validation, shell syntax, script permissions, and self-check gates passed. The release candidate’s signed ABI APKs are documented as successfully produced by GitHub Actions run `32630539974`.

## Critical Risks

No new critical or high static security issue was identified. The certification gate cannot establish runtime JNI loading, Telegram authentication, real upload delivery, lifecycle recovery, or device UI behavior without a connected device/emulator and a controlled Telegram test environment.

## Known Limitations

Local release APKs and the local AAB are unsigned because release secrets are not present in the local environment. The published signed ABI APK evidence is documented separately. Instrumentation and device smoke testing were not available. Existing QA records also identify missing dedicated coverage for several persistence, queue recovery, and ViewModel/repository paths.

## Unverified Areas

Device installation, fresh/cold/warm startup, JNI `Client.create()`, Telegram authorization and session restoration, logout/re-authentication, destination loading, real TDLib upload and delivery, progress, pause/resume/cancel/retry, background execution, persistence across process death, navigation state restoration, Arabic RTL, dark mode, accessibility, and measured startup/memory/battery/throughput performance remain **NOT VERIFIED**.

## Final Decision

**NO-GO — RELEASE CANDIDATE REJECTED**

The rejection is evidence-based and limited to the strict production gate. The candidate passes static/build/security/CI checks, but unrestricted production approval is not justified while the critical runtime paths for installation, authentication, upload delivery, background recovery, and device behavior remain unverified. This is not a claim that those paths are broken; it is a failure to meet the required evidence threshold.
