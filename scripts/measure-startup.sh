#!/usr/bin/env bash
# Measure cold and warm startup for Telegram Drive Uploader on a physical device.
#
# Why this script exists:
#   StartupBenchmark uses androidx MacrobenchmarkRule, which *deliberately refuses to run
#   on an emulator*. On an emulator it raises
#     org.junit.AssumptionViolatedException: got: <false>, expected: is <true>
#       at MacrobenchmarkRule$apply$1.evaluate(MacrobenchmarkRule.kt:210)
#   Emulator timings carry more noise than signal, so the rule skips rather than
#   publishing a meaningless number. Startup figures therefore require real hardware.
#
# Usage:
#   ./scripts/measure-startup.sh            # run both cold and warm
#   ./scripts/measure-startup.sh cold       # cold only
#   ./scripts/measure-startup.sh warm       # warm only
#
# Requirements:
#   * A physical Android device with USB debugging enabled and visible to `adb devices`
#   * minSdk is 30 (Android 11); macrobenchmark needs a device this new or newer
#   * The screen must stay awake and the device must not be in power-save
#
# Output:
#   Console summary, plus JSON/XML under:
#     %TEMP%\tdg-build\benchmark\build\outputs\androidTest-results\connected\nonMinifiedRelease\
#   On success the timing lines look like:
#     timeToInitialDisplayMs   P50 / P90 / P95 / P99
#     timeToFullDisplayMs      P50 / P90 / P95 / P99

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

ADB="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}/platform-tools/adb"
CLASS="com.telegramdrive.uploader.benchmark.StartupBenchmark"

if [[ ! -x "$ADB" ]]; then
  echo "adb not found at: $ADB" >&2
  echo "Set ANDROID_HOME (or ANDROID_SDK_ROOT) to your Android SDK location." >&2
  exit 1
fi

which_filter="${1:-both}"

# --- device selection -------------------------------------------------------
# An emulator serial is always "emulator-<port>". Refuse it with the real reason
# rather than letting the user stare at an AssumptionViolatedException.
mapfile -t devices < <("$ADB" devices | awk 'NR>1 && $2=="device" {print $1}')

if [[ ${#devices[@]} -eq 0 ]]; then
  echo "No device is connected. Connect a phone with USB debugging enabled." >&2
  echo "Current 'adb devices' output:" >&2
  "$ADB" devices >&2
  exit 1
fi

target=""
for serial in "${devices[@]}"; do
  if [[ "$serial" != emulator-* ]]; then
    target="$serial"
    break
  fi
done

if [[ -z "$target" ]]; then
  cat >&2 <<'MSG'
Only emulator(s) are connected. StartupBenchmark cannot run here.

androidx MacrobenchmarkRule skips emulators on purpose, because emulator startup
timings measure the host rather than the app.

Connect a physical Android device (Android 11+), enable USB debugging, then run
this script again. To confirm it is visible:

  adb devices
MSG
  exit 2
fi

echo "Using device: $target"
"$ADB" -s "$target" shell getprop ro.product.model
"$ADB" -s "$target" shell getprop ro.build.version.sdk | sed 's/^/API level: /'

# Keep the screen on so a timeout does not look like a slow start.
"$ADB" -s "$target" shell svc power stayon true >/dev/null 2>&1 || true

# --- run --------------------------------------------------------------------
gradle_args=(
  :benchmark:connectedNonMinifiedReleaseAndroidTest
  "-Pandroid.testInstrumentationRunnerArguments.class=$CLASS"
)
if [[ "$which_filter" != "both" ]]; then
  gradle_args+=("-Pandroid.testInstrumentationRunnerArguments.notClass=com.telegramdrive.uploader.benchmark.BaselineProfileGenerator")
fi

# ANDROID_SERIAL makes Gradle's device provider pick the right device when a
# physical phone and an emulator are connected at the same time.
echo
echo "Running Macrobenchmark (this takes a few minutes)..."
ANDROID_SERIAL="$target" ./gradlew "${gradle_args[@]}" --console=plain

# --- report -----------------------------------------------------------------
results_dir="$ROOT_DIR/benchmark/build/outputs/androidTest-results/connected/nonMinifiedRelease"
echo
echo "=================== Startup results ==================="
found=0
for report in "$results_dir"/*.xml; do
  [[ -e "$report" ]] || continue
  # timeToInitialDisplayMs / timeToFullDisplayMs percentiles appear as text.
  if grep -q "timeToInitialDisplayMs" "$report" 2>/dev/null; then
    found=1
    echo "--- $(basename "$report") ---"
    grep -oE "timeTo(Initial|Full)DisplayMs[^<]*" "$report" | head -8
  fi
done

if [[ $found -eq 0 ]]; then
  echo "No startup timings in the report. That means the run was skipped rather than"
  echo "measured. Check the XML for AssumptionViolatedException, which means the rule"
  echo "rejected the device."
  echo "Report directory: $results_dir"
fi
echo "======================================================"

"$ADB" -s "$target" shell svc power stayon false >/dev/null 2>&1 || true