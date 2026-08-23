# Final Build Matrix

| Build | Command | Result | Verified |
|---|---|---|---:|
| Debug | `gradle :app:assembleDebug` or `./gradlew :app:assembleDebug` | NOT VERIFIED — Gradle tooling unavailable locally | No |
| Release APK | `gradle :app:assembleRelease` or `./gradlew :app:assembleRelease` | NOT VERIFIED — signing/tooling gate | No |
| Release AAB | `gradle :app:bundleRelease` or `./gradlew :app:bundleRelease` | NOT VERIFIED — task/build/signing gate | No |
| Unit tests | `gradle :app:testDebugUnitTest` | NOT VERIFIED locally | No |
| Lint | `gradle :app:lintVitalRelease` | NOT VERIFIED locally | No |
| TDLib artifacts | `./scripts/check-tdlib-artifacts.sh` | FAIL locally: `readelf` unavailable for exact ELF checks | No |
