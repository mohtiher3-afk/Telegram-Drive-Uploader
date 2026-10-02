# Test Environment

| Component | Current repository configuration | Evidence / note |
|---|---|---|
| Java | CI uses Temurin JDK 17; source/target compatibility is Java 11 | `.github/workflows/gradle.yml`, `app/build.gradle.kts` |
| Android Gradle Plugin | Declared in repository version catalog/build configuration | Do not upgrade during QA phase |
| Gradle | CI uses Gradle 8.9 | Local checkout has no `gradlew` and sandbox has no standalone `gradle` executable |
| Android SDK | CI installs platform 36 and build tools 36.0.0 | Android 16/API 36 target path |
| NDK | CI installs 26.3.11579264 | Used for official native dependency preparation |
| Kotlin / Compose | Existing project versions retained | No dependency upgrades introduced |
| Unit test framework | Existing JUnit-based `app/src/test` suite | 14 `@Test` methods found in source inventory |
| Instrumentation | AndroidX/JUnit4 `TdLibRuntimeSmokeTest` | Requires emulator/device and native artifacts |
| WorkManager | Production WorkManager is configured; no dedicated test harness found | TestDriver coverage remains a gap |
| Room/DataStore | Production Room and DataStore exist | Isolated test suites not found in current inventory |

The authoritative reproducible commands are documented in `QA_GUIDE.md`. Runtime and release claims must identify whether they came from CI, an emulator, or a physical device.
