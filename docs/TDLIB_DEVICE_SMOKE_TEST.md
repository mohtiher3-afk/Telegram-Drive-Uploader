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

The exact output paths depend on the Android Gradle Plugin and ABI split configuration. Locate both files below the Gradle build directory:

```bash
find app/build/outputs -type f -name '*.apk' -print
```

Select the application APK matching the device ABI and the instrumentation APK whose name contains `androidTest`. Do not assume a fixed `outputs/apk/androidTest/debug/` path.

## Run on a connected device or emulator

Enable USB debugging on a physical device or start an emulator, then confirm that `adb devices` reports the target as `device`, not `unauthorized` or `offline`.

```bash
adb devices -l

APP_APK="$(find app/build/outputs -type f \( -name 'app-arm64-v8a-debug.apk' -o -name 'app-debug.apk' \) | head -n 1)"
TEST_APK="$(find app/build/outputs -type f -name '*androidTest*.apk' | head -n 1)"
test -n "$APP_APK" && test -n "$TEST_APK"

./scripts/run-tdlib-device-smoke-test.sh \
  --apk "$APP_APK" \
  --test-apk "$TEST_APK" \
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

The repository workflow uses a standard `ubuntu-24.04` hosted runner and `reactivecircus/android-emulator-runner` to launch an x86_64 emulator. The workflow must not use an unregistered custom label such as `ubuntu-24.04-4core`, because that leaves the job queued indefinitely. The runner must provide `adb`, an online emulator, and an API level compatible with the project’s `minSdk`. Do not treat a hosted CI build without an attached device or emulator as JNI runtime evidence.

A minimal CI sequence is:

```bash
gradle --no-daemon --max-workers=1 \
  -PtargetAbi=${ABI} \
  :app:assembleDebug :app:assembleDebugAndroidTest

BUILD_ROOT="${GITHUB_WORKSPACE:-$PWD}/app/build"
TEST_APK="$(find "$BUILD_ROOT" -type f -name '*androidTest*.apk' | head -n 1)"
APP_APK="$(find "$BUILD_ROOT" -type f \( -name "app-${ABI}-debug.apk" -o -name 'app-debug.apk' \) | head -n 1)"
test -n "$TEST_APK" && test -n "$APP_APK"

./scripts/run-tdlib-device-smoke-test.sh \
  --apk "$APP_APK" \
  --test-apk "$TEST_APK" \
  --serial "$ANDROID_SERIAL" \
  --log-dir "artifacts/tdlib-smoke/${ABI}"
```

The smoke test deliberately fails closed. It must not be changed to report success when the native library is absent, when `Client.create()` throws, or when only the instrumentation runner exits successfully without the explicit Logcat markers.
