#!/usr/bin/env bash
set -euo pipefail

PACKAGE="com.telegramdrive.uploader"
TEST_CLASS="com.telegramdrive.uploader.tdlib.TdLibRuntimeSmokeTest"
SERIAL=""
APK=""
TEST_APK=""
LOG_DIR="${PWD}/artifacts/tdlib-smoke"

usage() {
  cat <<'USAGE'
Usage:
  scripts/run-tdlib-device-smoke-test.sh \
    --apk path/to/app-debug.apk \
    --test-apk path/to/app-debug-androidTest.apk \
    [--serial emulator-5554] [--log-dir artifacts/tdlib-smoke]

The APK and test APK must be built for the same ABI as the connected device.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --apk) APK="${2:?Missing value for --apk}"; shift 2 ;;
    --test-apk) TEST_APK="${2:?Missing value for --test-apk}"; shift 2 ;;
    --serial) SERIAL="${2:?Missing value for --serial}"; shift 2 ;;
    --log-dir) LOG_DIR="${2:?Missing value for --log-dir}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
  esac
done

if [[ -z "$APK" || -z "$TEST_APK" ]]; then
  usage >&2
  exit 2
fi

for file in "$APK" "$TEST_APK"; do
  [[ -f "$file" ]] || { echo "Missing APK: $file" >&2; exit 2; }
done
command -v adb >/dev/null 2>&1 || { echo "adb is required" >&2; exit 2; }
mkdir -p "$LOG_DIR"

ADB=(adb)
if [[ -n "$SERIAL" ]]; then
  ADB+=( -s "$SERIAL" )
fi

state=$("${ADB[@]}" get-state 2>/dev/null || true)
if [[ "$state" != "device" ]]; then
  echo "No online Android device/emulator selected. adb state: ${state:-none}" >&2
  "${ADB[@]}" devices -l >&2 || true
  exit 3
fi

serial_name=$("${ADB[@]}" get-serialno 2>/dev/null || true)
abi_list=$("${ADB[@]}" shell getprop ro.product.cpu.abilist 2>/dev/null | tr -d '\r')
echo "DEVICE_SERIAL=$serial_name"
echo "DEVICE_ABIS=$abi_list"
echo "APK=$APK"
echo "TEST_APK=$TEST_APK"

"${ADB[@]}" install -r -d "$APK" >/dev/null
"${ADB[@]}" install -r -d "$TEST_APK" >/dev/null
"${ADB[@]}" logcat -c

instrumentation_output="$LOG_DIR/instrumentation.txt"
logcat_output="$LOG_DIR/logcat.txt"
set +e
"${ADB[@]}" shell am instrument -w -r \
  -e class "$TEST_CLASS" \
  "${PACKAGE}.test/androidx.test.runner.AndroidJUnitRunner" \
  2>&1 | tee "$instrumentation_output"
instrumentation_status=${PIPESTATUS[0]}
set -e

"${ADB[@]}" logcat -d -v threadtime -s TdLibRuntimeSmokeTest:I '*:S' > "$logcat_output" || true

if (( instrumentation_status != 0 )); then
  echo "STATUS: DEVICE_TEST_RUN=false"
  echo "Instrumentation failed with exit code $instrumentation_status. See $instrumentation_output and $logcat_output." >&2
  exit 1
fi
if ! grep -q 'OK (1 test)' "$instrumentation_output"; then
  echo "STATUS: DEVICE_TEST_RUN=false"
  echo "Instrumentation did not report OK (1 test). See $instrumentation_output." >&2
  exit 1
fi
if ! grep -q 'JNI_LOAD_STATUS=PASS' "$logcat_output"; then
  echo "STATUS: JNI_LOAD_STATUS=FAIL"
  echo "JNI pass marker was not observed. See $logcat_output." >&2
  exit 1
fi
if ! grep -q 'CLIENT_CREATE_STATUS=PASS' "$logcat_output"; then
  echo "STATUS: CLIENT_CREATE_STATUS=FAIL"
  echo "Client.create pass marker was not observed. See $logcat_output." >&2
  exit 1
fi

echo "STATUS: DEVICE_TEST_RUN=true"
echo "STATUS: JNI_LOAD_STATUS=PASS"
echo "STATUS: CLIENT_CREATE_STATUS=PASS"
echo "LOGCAT=$logcat_output"
