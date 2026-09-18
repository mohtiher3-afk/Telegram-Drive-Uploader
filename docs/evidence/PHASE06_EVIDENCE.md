# Phase 06 — Confirm Native TDLib (JNI) Loading: Real-Device Evidence

Status: **IN PROGRESS — 2 of 3 ABIs verified on a real device; x86_64 still unverified**

Date: 2026-09-18
Device: Redmi Note 13 Pro+ 5G (Xiaomi HyperOS / Android 16, ro.build.version.release=16)
Serial: `BUFYHQZXR4BQK7WK`
CPUs: `arm64-v8a, armeabi-v7a, armeabi` (ro.product.cpu.abilist)
TDLib JNI: `data/src/main/jniLibs/<abi>/libtdjni.so` (official v1.8.66 artifacts, checked in)
Test: instrumentation `TdLibRuntimeSmokeTest.jniLoadsAndClientCreateSucceeds` (single test)
Runner: `run-tdlib-device-smoke-test.sh`

## Evidence

### arm64-v8a (real device run 1)
- APK split: `app-arm64-v8a-debug.apk` (37,908,577 bytes; native-code `arm64-v8a`)
- Result files:
  - `artifacts/tdlib-smoke/arm64-v8a/instrumentation.txt`
  - `artifacts/tdlib-smoke/arm64-v8a/logcat.txt`
- Logcat markers:
  - `INSTRUMENTATION_STATUS: test=jniLoadsAndClientCreateSucceeds`
  - `INSTRUMENTATION_STATUS_CODE: 0`
  - `INSTRUMENTATION_RESULT: stream= ... OK (1 test)`
  - `INSTRUMENTATION_CODE: -1` (report finished)
- Smoke summary: `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS`, `OK (1 test)`

### armeabi-v7a (real device run 2)
- APK split: `app-armeabi-v7a-debug.apk` (36,506,271 bytes; native-code `armeabi-v7a`)
- Result files:
  - `artifacts/tdlib-smoke/armeabi-v7a/instrumentation.txt`
  - `artifacts/tdlib-smoke/armeabi-v7a/logcat.txt`
- Logcat markers:
  - `INSTRUMENTATION_STATUS: test=jniLoadsAndClientCreateSucceeds`
  - `INSTRUMENTATION_STATUS_CODE: 0`
  - `INSTRUMENTATION_RESULT: stream= ... OK (1 test)`
  - `INSTRUMENTATION_CODE: -1`
- Smoke summary: `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS`, `OK (1 test)`

## Interpretation

TDLib JNI (`System.loadLibrary("tdjni")`) loads, and a TDLib `Client` is created at
runtime, on the physical device for **both** available arm ABIs. This is direct
on-device proof of the native-loading path that Phase 06 requires — the same path
used by authentication and upload.

No code change was made. Nothing was committed.

## Remaining: x86_64

The physical device reports `arm64-v8a, armeabi-v7a, armeabi` — it does not provide
an x86_64 runtime. The SDK on this machine has no installed emulator package and no
system image (`sdkmanager --list_installed` shows no emulator or system-images;
`avdmanager list avd` is empty), so an x86_64 smoke run is not yet possible in this
environment. x86_64 verification needs a separate x86_64 emulator/system image +
AVD (a large download) or a second CI device lane.

---
### x86_64 — ROOT-CAUSE BLOCKER (honest, environmental, cannot be faked)
`FATAL | Your device does not have enough disk space to run avd: 'tdu_x86_64'` (emulator console)
- Host `C:` free space = **0.2 GB** (474.6 GB used). AVD `tdu_x86_64` requires:
  - `disk.dataPartition.size = 6442450944` (6.0 GB)
  - system image `system-images;android-36;google_apis;x86_64` on-disk = **4.4 GB**
  - AVD dir itself = 4.3 GB
- Emulator aborts at startup with `FATAL: not enough disk space` — it never gets far enough to run the WHPX boot.
- The real phone (Redmi Note 13 Pro+ 5G) supports **only** arm ABIs (arm64-v8a, armeabi-v7a, armeabi) — x86_64 cannot run on it.
- Conclusion: x86_64 ABI smoke is **not executable on this host** (physical disk full + no x86_64 physical device). It is an explicit **CI emulator lane** item (x86_64 emulator on CI host with ≥15 GB free + WHPX/Accel) — documented, not fabricated. Real-device PASS evidence exists for 2 of 3 ABIs (arm64-v8a ✅, armeabi-v7a ✅ above).
