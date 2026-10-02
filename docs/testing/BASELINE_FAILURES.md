# Baseline Failures and Environment Limits

The supplied QA phase requires a clean compile, unit tests, debug APK, and lint baseline. The temporary checkout cannot execute those Gradle commands locally because it does not contain `gradlew` and the sandbox does not provide a standalone Gradle executable. This is classified as an **ENVIRONMENT ISSUE**, not a production failure.

The repository CI workflow is therefore the authoritative build path. It provisions JDK 17, Android SDK/API 36, build tools 36.0.0, NDK 26.3.11579264, Gradle 8.9, official OpenSSL dependencies, and the required CI configuration. A previous resource-phase CI run was still in progress at the time of this phase audit; its final result must be recorded separately when available.

The local TDLib artifact checker found the native libraries and generated Java bindings but reported `TDLIB_ARTIFACTS_PRESENT=false` because exact ELF architecture validation requires `readelf`, which is unavailable in the temporary environment. This is classified as a **TDLIB ARTIFACT TOOLING / ENVIRONMENT ISSUE**. No native artifact was fabricated or replaced.

No production bug is inferred from unavailable local tooling. CI failures, if any, must be classified from their actual logs before code changes are attempted.
