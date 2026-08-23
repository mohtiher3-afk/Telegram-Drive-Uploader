# Current Toolchain Baseline

**Status: CURRENT — documentation only. No dependency update is approved or applied.**

| Component | Current value | Evidence |
|---|---|---|
| Java/JDK | JDK 17 used for local Android validation | Local validation environment; Gradle requires a complete JDK with `jlink`. |
| Gradle | 8.9 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.7.3 | `gradle/libs.versions.toml` |
| Kotlin | 2.2.10 | `gradle/libs.versions.toml` |
| Compose BOM | 2024.09.00 | `gradle/libs.versions.toml` |
| compileSdk | 36 | `app/build.gradle.kts` |
| targetSdk | 36 | `app/build.gradle.kts` |
| minSdk | 24 | `app/build.gradle.kts` |
| NDK | 26.3.11579264 in the existing native artifact workflow | TDLib artifact/build documentation |
| TDLib | 1.8.66 | TDLib artifact manifest and validation script |
| Supported ABIs | arm64-v8a, armeabi-v7a, x86_64 | `app/build.gradle.kts` and TDLib artifact validation |

This baseline is a reference for future change requests. It does not authorize upgrading any component. A requested update must first record its exact current and target versions, reason, compatibility evidence, and rollback plan.
