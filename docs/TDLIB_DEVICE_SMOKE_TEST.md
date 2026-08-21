# TDLib Device Runtime Smoke Test

This project includes an instrumentation test and an `adb` harness for verifying the two runtime prerequisites that a successful Gradle build cannot prove: loading the official `libtdjni.so` for the device ABI and creating a real `org.drinkless.tdlib.Client` instance.

The test does not authenticate a Telegram account and does not require API credentials. After `Client.create()` succeeds, it sends `TdApi.Close` so the smoke test does not leave an active TDLib client running. Authentication, `SetTdlibParameters`, and end-to-end upload behavior remain separate runtime tests.

## Build the matching APKs

Build a debug APK and its instrumentation APK for the same ABI as the connected device or emulator. For example, with Gradle 8.9 installed:

```bash
gradle --no-daemon --max-workers=1 \
  -PtargetAbi=arm64-v8a \
  :app:assembleDebug :app:assembleDebugAndroidTest
```

The expected files are normally:

```text
app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
```

The exact test APK filename can vary by Android Gradle Plugin version; locate it under `app/build/outputs/apk/androidTest/debug/` when necessary.

## Run on a connected device or emulator

Enable USB debugging on a physical device or start an emulator, then confirm that `adb devices` reports the target as `device`, not `unauthorized` or `offline`.

```bash
adb devices -l

./scripts/run-tdlib-device-smoke-test.sh \
  --apk app/build/outputs/apk/debug/app-arm64-v8a-debug.apk \
  --test-apk app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
  --serial emulator-5554
```

The `--serial` argument is optional when exactly one device is connected. The script installs both APKs, clears Logcat, runs only `TdLibRuntimeSmokeTest`, stores evidence under `artifacts/tdlib-smoke/`, and fails if any required status marker is absent.

## Expected result

A passing run prints:

```text
STATUS: DEVICE_TEST_RUN=true
STATUS: JNI_LOAD_STATUS=PASS
STATUS: CLIENT_CREATE_STATUS=PASS
```

The instrumentation test also emits `AUTH_STATE=...` if TDLib produces an authorization update. That value is informational for this smoke test; no Telegram login is attempted.

| Result | Meaning | Next action |
|---|---|---|
| `JNI_LOAD_STATUS=PASS` | Android loaded `libtdjni.so` for the selected device ABI | Continue to `Client.create()` verification |
| `JNI_LOAD_STATUS=FAIL` | Native loading failed, commonly because of wrong ABI, missing dependency, packaging, or R8/loader issues | Inspect `logcat.txt`, APK native entries, and `readelf -h` output |
| `CLIENT_CREATE_STATUS=PASS` | TDLib JNI bindings created a real native client | Authentication and parameter tests may proceed |
| `CLIENT_CREATE_STATUS=FAIL` | JNI loaded but TDLib client construction failed | Inspect native crash output and TDLib/R8 compatibility |
| `DEVICE_TEST_RUN=false` | `adb` or instrumentation infrastructure failed | Fix device, package, runner, or APK installation issues first |

## CI integration

A self-hosted GitHub Actions runner with an Android emulator can run the same harness after building the ABI-specific debug and test APKs. The runner must provide `adb`, an online emulator, and an API level compatible with the project’s `minSdk`. Do not treat a hosted CI build without an attached device or emulator as JNI runtime evidence.

A minimal CI sequence is:

```bash
gradle --no-daemon --max-workers=1 \
  -PtargetAbi=${ABI} \
  :app:assembleDebug :app:assembleDebugAndroidTest

./scripts/run-tdlib-device-smoke-test.sh \
  --apk "app/build/outputs/apk/debug/app-${ABI}-debug.apk" \
  --test-apk "$(find app/build/outputs/apk/androidTest/debug -type f -name '*.apk' | head -n 1)" \
  --serial "$ANDROID_SERIAL" \
  --log-dir "artifacts/tdlib-smoke/${ABI}"
```

The smoke test deliberately fails closed. It must not be changed to report success when the native library is absent, when `Client.create()` throws, or when only the instrumentation runner exits successfully without the explicit Logcat markers.
