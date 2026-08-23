# Dependency Impact Analysis

## Risk Classification

**Risk:** `LOW | MEDIUM | HIGH | CRITICAL`

Classify NDK/CMake, TDLib, JNI, ABI, authentication, Upload Engine, WorkManager, database, or security-architecture effects as at least HIGH unless evidence supports a different classification.

## Affected Components

| Component | Affected? | Risk | Evidence and impact |
|---|---:|---|---|
| Gradle/AGP/Java | No / Yes | `<risk>` | `<impact>` |
| Kotlin/compiler/plugins | No / Yes | `<risk>` | `<impact>` |
| Compose/Material 3/UI | No / Yes | `<risk>` | `<impact>` |
| AndroidX/Navigation/Hilt | No / Yes | `<risk>` | `<impact>` |
| Room/DataStore | No / Yes | `<risk>` | `<impact>` |
| WorkManager | No / Yes | `<risk>` | `<impact>` |
| Telegram/TDLib | No / Yes | `<risk>` | `<impact>` |
| JNI/ABI/native toolchain | No / Yes | `<risk>` | `<impact>` |
| Upload/authentication | No / Yes | `<risk>` | `<impact>` |
| Tests/security/release | No / Yes | `<risk>` | `<impact>` |

## Update Strategy

`SINGLE DEPENDENCY UPDATE | COMPATIBILITY GROUP UPDATE`

Explain why the selected group must move together and why unrelated libraries are excluded.

## Stop Conditions

State which failures require stopping: unexplained build failure, test regression, security issue, TDLib coupling, native incompatibility, or undefined rollback.
