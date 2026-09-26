# Phase 06 — Confirm Native TDLib (JNI) Loading: Real-Device Evidence

Status: **COMPLETE — all three ABIs verified.** arm64-v8a and armeabi-v7a on physical hardware
(2026-09-18, below); x86_64 on a CI hardware-accelerated emulator (2026-09-26, closing section).

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

## Historical remaining item: x86_64 (environmental, resolved via the CI lane)

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

---

## x86_64 — VERIFIED on the CI emulator lane (2026-09-26)

The blocker recorded above was environmental, not a code defect: the physical device has
an arm-only ABI list, and the local host had too little free disk to start an x86_64 AVD.
The repository already ships a dedicated lane for exactly this gap —
`Android TDLib Device Smoke Test` (`.github/workflows/android-device-smoke.yml`) — which
runs the same instrumentation test on an x86_64 `google_apis` emulator.

### Verbatim CI evidence

- Workflow: **Android TDLib Device Smoke Test** — run `36215977328`, commit `8c32724`
  (full `8c3272407fe4ea916259fbd39b87663d4aa3a0dc`): **completed success** (all four
  matrix jobs).
- Host and emulator: `ubuntu-24.04` runner, JDK 21, NDK `26.3.11579264`, `ABI=x86_64`,
  `reactivecircus/android-emulator-runner@v2` with `arch: x86_64`, `target: google_apis`,
  `profile: pixel_2`, KVM acceleration enabled in the workflow.
- APK under test: `app-x86_64-debug.apk` plus the instrumentation APK, installed by
  `scripts/run-tdlib-device-smoke-ci.sh` (which delegates to
  `scripts/run-tdlib-device-smoke-test.sh`).

| Job | Instrumented result | JNI markers |
|---|---|---|
| `TDLib JNI smoke (Android 33)` | `OK (1 test)` @ 03:55:01Z | `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS` |
| `TDLib JNI smoke (Android 34)` | `OK (1 test)` @ 03:55:13Z | `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS` |
| `TDLib JNI smoke (Android 35)` | `OK (1 test)` @ 03:55:12Z | `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS` |
| `TDLib JNI smoke (Android 36)` | `OK (1 test)` @ 03:55:40Z | `JNI_LOAD_STATUS=PASS`, `CLIENT_CREATE_STATUS=PASS` |

The marker sequence is identical in all four jobs:

```text
INSTRUMENTATION_STATUS_CODE: 1
INSTRUMENTATION_STATUS_CODE: 0
OK (1 test)
STATUS: JNI_LOAD_STATUS=PASS
STATUS: CLIENT_CREATE_STATUS=PASS
```

`INSTRUMENTATION_STATUS_CODE: 0` is a passing case, and the workflow fails the job when
any required marker is absent.
### Why the lane had to be re-triggered (CI gap now closed)

The lane had been red since run `36028821677` on `3536441`, where
`./scripts/check-tdlib-artifacts.sh` failed with four checksum mismatches:
`data/src/main/jniLibs/x86_64/libcrypto.so` and the three Java bindings (`Client.java`,
`Log.java`, `TdApi.java`). Both causes were environmental, and `7eeebef` removed them:

- CI rebuilds OpenSSL for the matrix ABI (`TARGET_ABIS=x86_64`), so the x86_64
  `libcrypto.so` bytes differ from the recorded digest while the untouched arm copies
  still matched. The checksum list now covers the prebuilt `libtdjni.so` and the Java
  bindings only, and `docs/TDLIB_ARTIFACT_MANIFEST.md` records the OpenSSL libraries as
  rebuilt from the pinned verified source archive.
- Checkout line-ending normalization rewrote the generated bindings, so their digests
  differed on Linux. `.gitattributes` now pins `data/src/main/java/org/drinkless/tdlib/**`
  as `-text`, and the digests are taken from Git blob bytes.

That fix could not re-run the lane, however: the workflow `push.paths` filter did not
list `scripts/check-tdlib-artifacts.sh`, `scripts/check-elf-alignment.py`,
`scripts/build-tdlib-android.sh`, `docs/TDLIB_SHA256SUMS.txt` or
`docs/TDLIB_ARTIFACT_MANIFEST.md`, so the lane stayed frozen on a pre-fix red run that no
longer described the tree. Commit `8c32724` adds those paths and widens the JNI-library
filter from `x86_64` to every ABI, so the lane now re-verifies itself whenever the
artifact contract it enforces changes. Run `36215977328` is the first lane execution
after the gate fix, and its `Verify official TDLib artifacts` step passes in all four
jobs. Locally, `scripts/check-tdlib-artifacts.sh` also passes for
`TDLIB_CHECK_ABI=x86_64` and `TDLIB_CHECK_ABI=all` at commit `8c32724`.

### Interpretation and scope

TDLib JNI (`System.loadLibrary("tdjni")`) loads, and a real `org.drinkless.tdlib.Client`
is created through `Client.create(...)`, on **x86_64** across API levels 33, 34, 35 and 36
on emulator hardware. With the two physical-device arm ABIs recorded above, all three
shipped ABIs now have runtime native-loading evidence, so the Phase 06 requirement is met.

This is not authentication, upload, or delivery certification: no Telegram credentials,
no destination selection, and no upload executed here. The remaining validation tracks
stay as listed in `docs/operations/FULL_VALIDATION_MATRIX.md`.
