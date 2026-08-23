# Final Build Matrix

| Build | Command | Result | Verified |
|---|---|---|---:|
| Debug | `./gradlew :app:assembleDebug` | PASS for arm64-v8a after local debug keystore setup | Yes |
| Release APK | `./gradlew :app:assembleRelease` | PASS for all three ABIs; outputs are unsigned | Yes: build only |
| Release AAB | `./gradlew :app:bundleRelease` | PASS; output is unsigned | Yes: build only |
| Unit tests | `./gradlew :app:testDebugUnitTest` | PASS | Yes |
| Lint | `./gradlew :app:lintVitalRelease` | PASS | Yes |
| TDLib artifacts | `./scripts/check-tdlib-artifacts.sh` | PASS after building pinned OpenSSL 3.0.16 runtime dependencies | Yes |
