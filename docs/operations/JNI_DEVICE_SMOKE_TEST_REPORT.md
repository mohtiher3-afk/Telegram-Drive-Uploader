# TDLib JNI Device Smoke-Test Report

**Repository:** [Telegram-Drive-Uploader](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader)

**Workflow:** [Android TDLib Device Smoke Test](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/workflows/android-device-smoke.yml)

**Run:** [32695682474](https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/32695682474)

**Commit under test:** `6179c37edb8266291ca9836e242fda9268fc376f`

**Date:** 2026-08-24

## Result

The dedicated GitHub-hosted Android emulator smoke test completed successfully. The workflow used a standard `ubuntu-24.04` hosted runner, JDK 17, Android SDK Platform 36, Build-Tools 36.0.0, NDK 26.3.11579264, the x86_64 ABI, API level 35, a `google_apis` Pixel 2 emulator profile, KVM acceleration, and disabled emulator animations.

| Workflow stage | Result |
|---|---|
| Checkout, JDK, Android SDK, and Gradle setup | PASS |
| OpenSSL Android runtime preparation for x86_64 | PASS |
| Official TDLib artifact validation | PASS |
| x86_64 debug and instrumentation APK build | PASS |
| KVM acceleration setup | PASS |
| Cloud emulator boot and instrumentation execution | PASS |
| Runtime evidence artifact upload | PASS |
| Cleanup of temporary test configuration | PASS |
| Overall run | **SUCCESS** — approximately 5m 59s |

## Explicit runtime evidence

The downloaded artifact `tdlib-device-smoke-x86_64/x86_64/logcat.txt` contains the following markers:

```text
TdLibRuntimeSmokeTest: JNI_LOAD_STATUS=PASS
TdLibRuntimeSmokeTest: CLIENT_CREATE_STATUS=PASS
```

The instrumentation test called `System.loadLibrary("tdjni")`, created a genuine `org.drinkless.tdlib.Client` via `Client.create(...)`, asserted the client was non-null, and closed it with `TdApi.Close()`. It also recorded any authorization-state callback and native callback failure. No Telegram authentication, destination selection, or upload was performed by this smoke test, which is intentional: the test isolates native loading and TDLib client initialization.

## Workflow review

The workflow uses `runs-on: ubuntu-24.04`; it no longer uses the unavailable custom `ubuntu-24.04-4core` label that previously left the job waiting for a runner. It installs the SDK and NDK packages explicitly, verifies ABI-specific TDLib artifacts before building, uses the x86_64 emulator path, uploads logcat and instrumentation evidence with `if: always()`, and removes temporary `.env` and `local.properties` files in a cleanup step.

GitHub reported non-blocking maintenance annotations that several actions target Node.js 20 and that `setup-java@v4` is deprecated in favor of a newer action version. These annotations did not affect the successful run. They should be handled in a separate controlled workflow-maintenance change rather than mixed with TDLib runtime verification.

## Certification impact

This run provides positive device evidence for the **x86_64** native loading path and the basic TDLib `Client.create()` lifecycle on an API 35 Google APIs emulator. It does not certify arm64-v8a or armeabi-v7a runtime loading, Android 16 behavior, Telegram authentication, channel discovery, destination permissions, WorkManager execution, real upload delivery, progress telemetry, cancellation, history projection, or UI accessibility. Those remain separate validation tracks.

The project can record **JNI x86_64 smoke test: PASS** while retaining **overall release certification: NO-GO / NOT CERTIFIED** until the broader device and real-upload evidence is collected.

## References

[1]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/actions/runs/32695682474 "GitHub Actions run 32695682474"
[2]: https://github.com/mohtiher3-afk/Telegram-Drive-Uploader/blob/main/.github/workflows/android-device-smoke.yml "Android device smoke workflow"
[3]: https://developer.android.com/studio/run/emulator "Android Emulator — Android Developers"
