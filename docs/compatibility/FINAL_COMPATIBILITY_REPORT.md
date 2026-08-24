# Final Android Compatibility Report

## Scope

This review documents configured Android support and the available validation evidence. It does not certify devices, OEMs, screen classes, or runtime flows that were not tested.

## Configured Support

The application is configured with minSdk 24, compileSdk 36, targetSdk 36, versionCode 15, and versionName 1.0.15. Native artifacts are present for `arm64-v8a`, `armeabi-v7a`, and `x86_64`. English and Arabic resources exist, and the manifest declares RTL support.

## Build and Native Evidence

The repository build, lint, JVM tests, security checks, and TDLib artifact checks pass locally. The official TDLib v1.8.66 Java/native artifacts and required ABI libraries are present. This evidence is static/build evidence and is not a substitute for device JNI loading or runtime upload testing.

## Runtime Matrix

Android API classes 24–28, 29–32, 33–35, and API 36 remain runtime `NOT TESTED` in this environment. Install, startup, Telegram authentication, upload, background execution, notifications, UI, RTL, and configuration-change results are therefore not certified.

Representative small-phone, normal-phone, large-phone, tablet, foldable, and low-memory classes are documented in `DEVICE_AND_SCREEN_MATRIX.md` but remain untested. Screen clipping, overlap, long filenames/chat names, dialogs, progress, IME, font scale, dark mode, and RTL also require device evidence.

## Platform Boundaries

Edge-to-edge is configured through `enableEdgeToEdge()` with Compose inset handling. No orientation lock was observed. WorkManager and `WAKE_LOCK` are used for background behavior. These configurations require runtime verification across API and OEM conditions; no device-specific workaround was added.

## Permissions

The actual manifest permissions are `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_MEDIA_VIDEO`, API-32-conditional `READ_EXTERNAL_STORAGE`, and `WAKE_LOCK`. No permission was added or removed during this review.

## Known Limitations

There is no connected Android device or emulator evidence for this phase. As a result, this report makes no claim of universal compatibility, uninterrupted background execution, successful runtime authentication, real upload delivery, notification correctness on every API/OEM, or successful native loading on every ABI device.

## Release Relationship

The existing v1.0.15 release certification remains **NO-GO** for unrestricted production runtime because the required real-device evidence is missing. This compatibility review does not change that decision.

## Final Status

**COMPATIBILITY DOCUMENTED — RUNTIME CERTIFICATION PENDING**.

No application source, dependency, TDLib, JNI, ABI, permission, orientation, window, upload, database, or release behavior was changed.
