# CI Guide

The canonical workflow is `.github/workflows/android-ci.yml`. It runs on pushes and pull requests targeting `main`, uses least-privilege read permissions, provisions JDK 17, Android API 36/build tools 36.0.0/NDK 26.3.11579264, prepares the existing official OpenSSL runtime dependency, validates TDLib artifacts, validates WorkManager and resources, runs JVM tests and release lint, and assembles a debug APK for each supported ABI.

The security job runs a redacted repository scan without printing match contents. No signing or Telegram secrets are exposed to pull-request jobs. Release signing is isolated in the manually dispatched release workflow and requires repository secrets; it does not publish to Google Play.

Run locally with `scripts/verify-project.sh` when the Gradle wrapper or a compatible `gradle` executable and Android SDK are available. The script fails closed when required tooling is missing. CI artifacts are limited to debug/release APKs, checksums, and useful test evidence; caches never contain user data, authentication sessions, or signing keys.

When a job fails, classify it as BUILD FAILURE, TEST FAILURE, LINT FAILURE, SECURITY FAILURE, TDLIB ARTIFACT FAILURE, or ENVIRONMENT FAILURE. Do not use `continue-on-error` for critical gates and do not replace missing native artifacts with placeholders.
