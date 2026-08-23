# QA Guide

## Test architecture

Use JVM unit tests for pure formatting, media recognition, policies, state decisions, and TDLib message mapping. Use controlled fakes at the Telegram boundary for authentication, chat lookup, upload requests, and error mapping. Use isolated Room/DataStore and WorkManager test facilities when adding those suites. Use Compose semantics-based tests for stable user-facing state and interaction. Reserve real Telegram accounts and files for manual device validation only.

## Commands

From a checkout with the Android toolchain available:

```text
./gradlew clean
./gradlew test
./gradlew :app:compileDebugKotlin
./gradlew :app:processDebugResources
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
./gradlew :app:lint
./gradlew :app:connectedDebugAndroidTest
./scripts/check-resource-integrity.sh
./scripts/check-workmanager-manifest.sh
./scripts/check-tdlib-artifacts.sh
```

The repository CI workflow provisions the required toolchain and runs ABI-specific artifact checks, JVM tests, release lint, and debug APK assembly. It does not prove real Telegram authorization or end-to-end upload delivery.

## Manual validation

On a fresh device install, validate onboarding, English and Arabic, light and dark themes, authentication, destination selection, supported file selection, queue insertion, real progress, confirmed completion/failure, history, settings, backgrounding, relaunch, and process-recreation behavior. Record the device API, ABI, app version, TDLib artifact status, and sanitized logs. Never include API hashes, tokens, phone numbers, codes, passwords, session data, or private file names.

## Failure handling

Classify failures as production bug, test bug, environment issue, missing dependency, TDLib artifact issue, device issue, or flaky test. Fix production failures only with a reproduction and regression test where practical. Do not change upload or authentication behavior to make a test pass.
