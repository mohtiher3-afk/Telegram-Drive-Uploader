# Baseline Failures and Environment Limits

The QA phase originally required a clean compile, unit tests, debug APK, and lint baseline. That phase ran in a sandboxed checkout that had no `gradlew` and no standalone Gradle executable, so those Gradle commands could not be executed there. This was classified as an **ENVIRONMENT ISSUE**, not a production failure. That sandbox constraint no longer applies: this repository ships the Gradle Wrapper, and `scripts/verify-project.ps1` (or `scripts/verify-project.sh`) runs the full gate locally.

The repository CI workflow is therefore not the only build path, though it remains authoritative for platform-specific coverage. It provisions JDK 21, Android SDK/API 34, build tools 34.0.0, NDK 26.3.11579264, and uses the committed Gradle Wrapper (9.6.0) via `gradle/actions/setup-gradle`.

The local TDLib artifact checker found the native libraries and generated Java bindings but reported `TDLIB_ARTIFACTS_PRESENT=false` because exact ELF architecture validation requires `readelf`, which is unavailable in the temporary environment. This is classified as a **TDLIB ARTIFACT TOOLING / ENVIRONMENT ISSUE**. No native artifact was fabricated or replaced.

No production bug is inferred from unavailable local tooling. CI failures, if any, must be classified from their actual logs before code changes are attempted.
