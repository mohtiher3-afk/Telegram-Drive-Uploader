# Dependency Inventory

**Status: CURRENT — documentation only. No dependency update is approved or applied.**

The repository centralizes versions in `gradle/libs.versions.toml`. The inventory below records the current declared versions and their maintenance criticality.

| Dependency family | Current version | Classification | Used by | Criticality | Update requested |
|---|---:|---|---|---|---|
| Android Gradle Plugin | 8.7.3 | BUILD_TOOL | Android build | High | No |
| Gradle Wrapper | 8.9 | BUILD_TOOL | Android build | High | No |
| Kotlin/compiler/plugins | 2.2.10 | BUILD_TOOL | Kotlin and Compose compilation | High | No |
| Compose BOM | 2024.09.00 | UI | Compose UI | High | No |
| Material 3 | Catalog-managed | UI | Design system | High | No |
| Navigation Compose | 2.8.9 | UI | Navigation | High | No |
| Hilt | 2.56.2 | BUILD_TOOL / RUNTIME | Dependency injection | High | No |
| Hilt navigation/work | 1.2.0 | RUNTIME | ViewModels and Workers | High | No |
| Room runtime/KTX/compiler | 2.7.0 | DATA | Persistence | High | No |
| DataStore Preferences | 1.1.7 | DATA | Settings persistence | High | No |
| WorkManager | 2.10.0 | BACKGROUND | Upload scheduling | High | No |
| Kotlin Coroutines | 1.10.2 | RUNTIME | Async and Flow behavior | High | No |
| Retrofit / Moshi | 2.12.0 / 1.15.2 | NETWORKING | Network and serialization paths | Medium | No |
| OkHttp / logging interceptor | 4.10.0 | NETWORKING | HTTP clients and diagnostics | High | No |
| Coil Compose | 2.7.0 | UI | Image loading | Medium | No |
| CameraX | 1.5.0 | OPTIONAL / UI | Camera-related paths | Medium | No |
| Accompanist permissions | 0.37.3 | UI / PERMISSIONS | Runtime permission UI | Medium | No |
| Google services / Firebase BOM | 4.4.3 / 34.15.0 | OPTIONAL / BUILD_TOOL | Google integration | Medium | No |
| TDLib/JNI/native artifacts | 1.8.66 | TELEGRAM / NATIVE | Telegram and uploads | Critical | No; use dedicated TDLib protocol |
| Android NDK/CMake | Existing native workflow | NATIVE | Native artifact compatibility | Critical | No |
| JUnit / AndroidX test / Espresso | 4.13.2 / 1.3.0 / 3.7.0 | TEST | JVM and Android tests | High | No |
| Robolectric / Roborazzi | 4.16.1 / 1.59.0 | TEST | Local and visual test support | Medium | No |

Transitive dependency inspection was performed through the Gradle debug runtime classpath. No dependency update or forced resolution was introduced in this phase. Versions that are not independently pinned in the catalog remain managed by their declared BOM or Gradle resolution strategy and must be recorded explicitly in any future change report.
