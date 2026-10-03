# Current Toolchain Baseline

**Status: CURRENT — documentation only. No dependency update is approved or applied.**

| Component | Current value | Evidence |
|---|---|---|
| Java/JDK | JDK 21 (Temurin 21.0.12) drives the build; Java 17 bytecode target | `app/build.gradle.kts` sets `sourceCompatibility`/`targetCompatibility` 17 and `jvmTarget` JVM_17 |
| Gradle | 9.6.0 | `gradle/wrapper/gradle-wrapper.properties` (`distributionUrl`) |
| Android Gradle Plugin | 9.4.1 | `gradle/libs.versions.toml` (`agp`) |
| Kotlin | 2.3.21 | `gradle/libs.versions.toml` (`kotlin`) |
| Compose BOM | 2026.09.00 | `gradle/libs.versions.toml` (`composeBom`) |
| compileSdk | 37 | `app/build.gradle.kts` |
| targetSdk | 36 | `app/build.gradle.kts` |
| minSdk | 30 in `:app`; 24 in the library modules | `app/build.gradle.kts` and the `core`/`data`/`feature` build files |
| NDK | 26.3.11579264 in the existing native artifact workflow | TDLib artifact/build documentation |
| TDLib | 1.8.66 | TDLib artifact manifest and validation script |
| Supported ABIs | arm64-v8a, armeabi-v7a, x86_64 | `app/build.gradle.kts` and TDLib artifact validation |

This baseline is a reference for future change requests. It does not authorize upgrading any component. A requested update must first record its exact current and target versions, reason, compatibility evidence, and rollback plan.
