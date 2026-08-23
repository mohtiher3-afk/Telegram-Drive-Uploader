# Developer Onboarding

## What the Project Does

Telegram Drive Uploader selects local media, authenticates through the official TDLib integration, and uploads files to an authorized Telegram destination through the real upload path. Queue and background execution are handled by the existing application architecture.

## How to Build

Use JDK 17, the committed Gradle Wrapper (8.9), Android API 36, Build Tools 36.0.0, and NDK 26.3.11579264. Run `./gradlew :app:assembleDebug` for a debug APK and use the manual Release workflow for signed multi-ABI artifacts.

## Telegram and TDLib

TDLib access is isolated behind `data.telegram`. Native artifacts and generated bindings must match the documented official version and ABI. Missing or invalid native artifacts must fail closed; never substitute mocks.

## Uploads

Uploads flow through the existing queue and worker architecture. Progress must reflect real TDLib updates, and completion must not be reported without Telegram confirmation.

## Tests and CI

Run the repository guards, `./gradlew :app:testDebugUnitTest`, release lint, and the relevant build task. CI runs security, resource, WorkManager, TDLib, JVM, lint, and multi-ABI gates. Device smoke tests require an emulator or physical device.

## Releases

A release requires a version, commit, tag, signed artifacts, checksums, verification, notes, limitations, and rollback reference. The Release workflow is manually dispatched and consumes GitHub Secrets without exposing their values.

## Documentation

Start at [README.md](README.md), then review [release/README.md](release/README.md), [operations/README.md](operations/README.md), architecture status, testing, performance, security, and CI reports.
