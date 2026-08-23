# Release Environment

| Item | Current value | Evidence/status |
|---|---|---|
| Java | 17 in CI; source compatibility Java 11 | `android-ci.yml`, `app/build.gradle.kts` |
| Gradle | 8.9 provisioned by CI | CI workflow; local wrapper absent |
| Android Gradle Plugin | Version catalog/build configuration | Repository source; final Gradle execution pending |
| Kotlin | Version catalog/build configuration | Repository source; final Gradle execution pending |
| compileSdk | 36 | `app/build.gradle.kts` |
| targetSdk | 36 | `app/build.gradle.kts` |
| minSdk | 24 | `app/build.gradle.kts` |
| NDK | 26.3.11579264 in CI | CI workflow |
| CMake | Used only by native dependency preparation where configured | Final native build execution pending |
| TDLib | Official v1.8.66 artifacts/bindings | Artifact guard; final run pending |
| ABIs | arm64-v8a, armeabi-v7a, x86_64 | CI matrix and packaged artifact layout |
| applicationId | `com.telegramdrive.uploader` | Gradle source |
| version | 1.0.14 / versionCode 14 | Gradle source |
