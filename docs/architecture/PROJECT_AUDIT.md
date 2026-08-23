# Project Audit — Telegram Drive Uploader

## Audit scope

This document records the current architecture of the repository at commit `eb7394f`. It is a planning artifact only. No Android source, Gradle configuration, TDLib bindings, native libraries, resources, navigation, WorkManager implementation, or upload behavior is changed by this architecture-planning phase.

## Current baseline

| Area | Current state | Evidence |
|---|---|---|
| Application | Telegram Drive Uploader | `app/build.gradle.kts`, `README.md` |
| Package | `com.telegramdrive.uploader` | `app/build.gradle.kts` |
| Version | 1.0.14 | `app/build.gradle.kts` |
| UI | Kotlin, Jetpack Compose, Material 3 | `app/src/main/java/.../core/ui`, feature screens |
| Persistence | Room database for upload state/history | `data/local/AppDatabase.kt`, `UploadDao.kt`, `UploadEntity.kt` |
| Background work | WorkManager with Hilt worker factory | `TelegramDriveApp.kt`, `core/di/WorkModule.kt`, `UploadWorker.kt` |
| Telegram | Official TDLib integration | `data/telegram/client`, `data/telegram/repository`, native artifact docs |
| Native ABIs | arm64-v8a, armeabi-v7a, x86_64 | CI matrix and artifact checker |
| Languages | English and Arabic resources | `res/values/strings.xml`, `res/values-ar/strings.xml` |
| CI | Multi-ABI build, tests, lint, artifact checks | `.github/workflows/gradle.yml` |

## Actual architecture

```text
Compose screen
    ↓
Hilt ViewModel
    ↓
Domain interface / use-oriented manager
    ↓
Repository or upload engine
    ↓
Room / WorkManager / Android file APIs / TDLib client
    ↓
Official TDLib native runtime
    ↓
Telegram network
```

The package structure is already organized by `core`, `data`, `domain`, and `feature`. The supplied target architecture is therefore a possible refinement, not a reason for a wholesale package move.

## Findings by severity

| Severity | Finding | Status | Planning implication |
|---|---|---|---|
| CRITICAL | Native TDLib loading and dependency integrity must remain fail-closed | Protected and CI-gated | Do not move or regenerate bindings/native artifacts during early refactoring |
| CRITICAL | WorkManager startup previously risked leaving user work queued | Repaired in v1.0.14 | Preserve manifest/provider guard and add worker lifecycle tests before refactoring |
| HIGH | Real authenticated Telegram delivery is not proven by CI | Open evidence gap | Requires controlled physical-device QA; no architecture move should obscure this |
| HIGH | Process-death, reboot, network restoration, and battery restriction recovery need device evidence | Open evidence gap | Add instrumented tests or documented manual QA before large changes |
| MEDIUM | Authentication, destination, and complete worker lifecycle test coverage is limited | Open coverage gap | Add focused tests before splitting managers or repositories |
| MEDIUM | Some manager/engine classes combine orchestration and state coordination | Review required | Split only with characterization tests and a reversible commit |
| LOW | Repository has no committed Gradle wrapper | Known constraint | Keep CI Gradle 8.9 setup stable; add wrapper separately if desired |
| IMPROVEMENT | Several architecture documents were previously absent | Addressed by this planning phase | Keep docs synchronized with actual code, not target aspirations |

## Protected areas

The following must not be changed during initial organization: `app/src/main/jniLibs/**`, official `org.drinkless.tdlib.*` bindings, TDLib artifact manifests/checkers, OpenSSL build scripts, `TelegramClientImpl.kt`, `UploadWorker.kt`, Room schema/migrations, `AndroidManifest.xml`, and release-signing configuration. Any change to these areas requires a separate risk review, focused tests, and a rollback point.

## Safe refactoring principle

The safest first implementation phase is test characterization and documentation, not moving files. The current code is already close to the proposed layering. A package move should be considered only when it removes a measured dependency violation or duplicated responsibility and when imports, Hilt bindings, tests, resources, and manifest references can be validated in one small commit.

## References

1. [Telegram Drive Uploader repository](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)
2. [Official TDLib repository](https://github.com/tdlib/td)
3. [Android WorkManager custom configuration](https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration)
