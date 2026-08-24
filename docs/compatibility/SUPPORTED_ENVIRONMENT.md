# Supported Environment

This record describes configured support boundaries, not a claim that every environment has been runtime-tested.

| Dimension | Configured value | Evidence | Status |
|---|---|---|---|
| Minimum Android | API 24 | `app/build.gradle.kts` | Configured |
| Target Android | API 36 | `app/build.gradle.kts` | Configured |
| Compile SDK | API 36 | `app/build.gradle.kts` | Configured |
| Application version | 1.0.15 / versionCode 15 | `app/build.gradle.kts` | Configured |
| Native ABIs | `arm64-v8a`, `armeabi-v7a`, `x86_64` | `app/src/main/jniLibs` and TDLib artifact checks | Artifacts present; runtime not fully tested |
| Languages | English default, Arabic resources | `values/`, `values-ar/`, `supportsRtl=true` | Resource support present; representative runtime matrix not complete |
| Orientation | No manifest lock observed | Manifest and source review | Runtime rotation not tested here |
| Edge-to-edge | `enableEdgeToEdge()` in `MainActivity` | `MainActivity.kt` | Configured; device/inset matrix not complete |
| Window/insets | Compose window-inset handling in navigation/onboarding | `AppNavigation.kt`, `OnboardingScreen.kt` | Static evidence only |
| Backup | Room database and DataStore excluded from backup/transfer | `backup_rules.xml`, `data_extraction_rules.xml` | Configured |
| Background execution | WorkManager and `WAKE_LOCK` present | Manifest and worker sources | Runtime background matrix not complete |

The configured range is API 24 through the target behavior of API 36. It does not constitute certification for all Android versions, OEMs, screen sizes, font scales, orientations, or device memory profiles.
