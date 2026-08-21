# Telegram Drive Uploader — Project Audit

**Audit date:** 2026-08-21  
**TDLib:** Official v1.8.66  
**Supported ABI:** `arm64-v8a`

## Executive status

The Android project now contains the official TDLib v1.8.66 ARM64 integration, a fail-closed native loading path, Arabic localization and RTL support, Material 3 Expressive UI work, first-run onboarding with real permission requests, and a local Smart File Assistant. The final bounded-memory Release build completed successfully, and the resulting APK contains the verified AArch64 `libtdjni.so` artifact.

## Completed verification

| Area | Result | Evidence |
|---|---|---|
| TDLib artifact gate | Passed | `STATUS: TDLIB_ARTIFACTS_PRESENT=true` |
| Native architecture | Passed | `file` reports ELF 64-bit AArch64 shared object |
| Java bindings | Passed | Official `Client.java` and `TdApi.java` bindings detected |
| Unit tests | Passed | `SmartFileAssistantTest`: 3 tests, 0 failures, 0 errors |
| Kotlin compiler configuration | Updated | Deprecated `kotlinOptions.jvmTarget` replaced by Kotlin compilerOptions DSL with JVM 11 |
| Release lint | Passed | `:app:lintVitalRelease` completed successfully |
| R8 release shrink | Passed | `:app:minifyReleaseWithR8` completed successfully |
| Release packaging | Passed | `:app:assembleRelease` completed successfully |
| ABI packaging | Passed | APK contains only `lib/arm64-v8a/libtdjni.so` by design |

## Fixes applied during the audit

| Area | Fix |
|---|---|
| Dual Wi-Fi | Guarded `WifiManager.isStaConcurrencyForLocalOnlyConnectionsSupported` with Android API 31 (`Build.VERSION_CODES.S`) to eliminate the `NewApi` lint error while retaining fail-safe behavior on older versions. |
| Unused AI dependencies | Removed unused Firebase AI, Firebase App Check, and Firebase BOM app dependencies. The implemented Smart File Assistant remains local and does not require a remote API key. |
| Notification permission | Removed `POST_NOTIFICATIONS` from the manifest and onboarding request because the app does not currently create notification channels or post upload notifications. |
| Arabic localization | Added Arabic string resources and localized the main Home, Upload, Queue, History, Settings, Telegram authentication, destination, onboarding, and shared-component labels. |
| RTL | Preserved `android:supportsRtl="true"`; Arabic resources follow the Android system locale and Compose layouts can render RTL. |
| TDLib | Preserved the official v1.8.66 Java bindings and ARM64 `libtdjni.so`; no mocks or fallback native implementation were introduced. |
| Local assistant tests | Added three JVM tests covering Arabic inference, English screen-recording inference, dimensions, duration metadata, and safe fallback naming. The test fixture uses the actual `UploadStatus.QUEUED` enum. |
| Kotlin warnings | Migrated the module from the deprecated `kotlinOptions { jvmTarget = "11" }` form to `compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }`. |

## Final Release artifact

| Item | Value |
|---|---:|
| APK | `app/build/outputs/apk/release/app-arm64-v8a-release.apk` |
| Size | 20,059,186 bytes |
| SHA-256 | `0ac3feda7b627a5f00c9935e99b8f7dcaccb84fbb5ebbed54af7fabef520859f` |
| Native library in APK | `lib/arm64-v8a/libtdjni.so` |
| Native library size | 58,944,152 bytes |
| Native library state | Stripped AArch64 ELF shared object |

The release APK is ARM64-only by design. It should not be installed on 32-bit ARM or x86 devices.

## Residual risks and device-only checks

A sandbox build cannot prove Telegram login or native runtime behavior on a physical handset. The following must still be exercised on a real ARM64 Android device: first-run onboarding, Arabic system locale and RTL layout, media picker access, phone and QR authentication, destination list population, upload to Saved Messages/group/channel, scheduled upload recovery after process restart, logout/login recovery, low-storage behavior, and network loss during upload.

The release build’s remaining Kotlin/R8 notices, if surfaced by a specific dependency version, are non-blocking compatibility warnings rather than compilation or packaging failures. A future dependency-maintenance pass may align the Kotlin, Android Gradle Plugin, Moshi, and KSP versions together; no broad version upgrade was made in this remediation because it would increase TDLib integration risk without being required for the verified build.

The native loader remains fail-closed when the library is absent or cannot be loaded. No mock TDLib implementation, fabricated authentication success, or fallback upload path was added.
