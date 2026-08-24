# Full Validation Matrix

**Date:** 2026-08-24

**Scope:** Evidence-based validation of the existing Telegram Drive Uploader. No mock TDLib, fake upload, placeholder native library, hard-coded credential, schema change, dependency upgrade, or business-logic rewrite is allowed.

## Validation tracks

| Track | Required evidence | Current entry point | Boundary |
|---|---|---|---|
| Native ABI loading | `JNI_LOAD_STATUS=PASS` and `CLIENT_CREATE_STATUS=PASS` on a device/emulator for each ABI | `.github/workflows/android-device-smoke.yml` and `TdLibRuntimeSmokeTest` | x86_64 hosted emulator is already supported; other ABIs require compatible runtime hardware or emulator architecture |
| Android 16 / API 36 | Same JNI markers on API 36 plus build/install evidence | Existing workflow currently uses API 35 emulator and API 36 compile SDK | API 36 runtime must be explicitly requested and available on the hosted runner |
| Authentication | TDLib authorization-state evidence for test account with credentials kept outside source/logs | `TelegramClientImpl`, `TelegramAuthScreen`, instrumentation/manual device flow | Requires user-controlled Telegram test account and a device; no credentials will be requested in chat or committed |
| Channel discovery and permissions | Search/list results, stable numeric chat ID, admin/write permission result, and selected destination persistence | `TelegramDestinationScreen`, `TelegramClientImpl`, destination tests | Requires an authorized account and controlled test channel; public links alone do not prove permissions |
| Real upload and WorkManager | `WORKER_STARTED`, TDLib `UpdateFile`, matching terminal message success, persisted `COMPLETED`, and artifact/log correlation | `UploadWorker`, `TelegramUploadEngineImpl`, TDLib callbacks | Requires explicit user-provided test file and controlled Telegram destination; enqueue alone is not proof |
| Multi-ABI packaging | ABI-specific APK contains only the intended `libtdjni.so`, passes artifact checker, and signed APK verification when CI secrets exist | `android-ci.yml`, `android-release.yml`, `check-tdlib-artifacts.sh` | Packaging evidence is separate from runtime loading evidence |
| Lint and CI | Lint error count, warning categories, workflow syntax, completed Actions jobs | Gradle lint and GitHub Actions | Dependency-update warnings are recorded, not upgraded in this validation phase |

## Safety boundary for real upload

The requested real-upload track cannot be truthfully executed using only repository access. It needs a user-controlled Telegram account already authorized in the app, a test channel where the account has permission to post, and a non-sensitive test file. The test must be performed on a controlled device or emulator with logs redacted. Until those prerequisites are explicitly provided through a safe device workflow, the agent will not send a file, post to a channel, or expose credentials.

## Certification rule

A successful build or a successful x86_64 smoke test does not certify all ABIs, Android 16, authentication, channel permissions, WorkManager execution, or real delivery. Each row receives one of four states: **repository verified**, **build verified**, **device/runtime verified**, or **not verified**.

## Planned order

The safe order is to inspect and validate the existing workflows, run build/artifact checks for all ABIs, run the existing x86_64 smoke path on API 36 if supported, audit auth and destination code statically, then pause before real upload unless controlled runtime prerequisites are available. Lint and CI warnings are reviewed last so unrelated maintenance is not mixed into the device evidence.

## Emulator-runner evidence

The official `reactivecircus/android-emulator-runner` documentation confirms that modern x86/x86_64 emulator images are the supported fast path on GitHub-hosted Linux runners with KVM, while ARM-based emulator images are more limited in API-level availability and are not the default hosted path [1]. This means x86_64 device evidence can be obtained reliably on the existing runner; arm64-v8a and armeabi-v7a still require explicit compatible emulator/image support or physical-device evidence rather than being inferred from packaging success.

The repository’s current smoke workflow already enables KVM, builds the selected x86_64 debug and instrumentation APKs, uses an API 35 Google APIs Pixel 2 emulator, and records runtime logcat evidence. API 36 compile SDK installation is not equivalent to API 36 runtime coverage, so an API 36 smoke run must be separately dispatched and its runner support observed.

[1]: https://github.com/ReactiveCircus/android-emulator-runner "ReactiveCircus Android Emulator Runner documentation"
