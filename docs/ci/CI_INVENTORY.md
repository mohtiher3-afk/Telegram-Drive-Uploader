# CI Inventory

| System | Exists | Location | Purpose | Status |
|---|---:|---|---|---|
| Primary Android CI | Yes | `.github/workflows/android-ci.yml` | Security, TDLib/resource/WorkManager checks, JVM tests, lint, ABI debug APKs | Configured |
| Device smoke workflow | Yes | `.github/workflows/android-device-smoke.yml` | x86_64 TDLib JNI smoke test on hosted emulator | Device-dependent |
| Release workflow | Yes | `.github/workflows/android-release.yml` | Manually dispatched signed multi-ABI release build and artifact publication | Manual only |
| TDLib artifact guard | Yes | `scripts/check-tdlib-artifacts.sh` | Fail when required official artifacts are absent or invalid | CI gate |
| Resource guard | Yes | `scripts/check-resource-integrity.sh` | Locale, manifest, icon, and duplicate-resource checks | CI gate |
| Security guard | Yes | `scripts/check-repository-security.sh` | Redacted private-key/token pattern scan | CI gate |
| Local equivalent | Yes | `scripts/verify-project.sh` | Repeats essential checks when Gradle/toolchain are available | Fail-closed |
| Gradle wrapper | No | `gradlew`, `gradle/wrapper/` | Wrapper-based reproducibility | Deferred; CI currently provisions Gradle 8.9 |
| Fastlane/Docker | No | N/A | Not required by current build | Not applicable |
