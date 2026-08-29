#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
BUILD_ROOT="$REPO_ROOT/app/build"
ABI="${ABI:-x86_64}"

TEST_APK=$(find "$BUILD_ROOT" -type f -name '*androidTest*.apk' | head -n 1)
APP_APK=$(find "$BUILD_ROOT" -type f \( -name "app-${ABI}-debug.apk" -o -name 'app-debug.apk' \) | head -n 1)

printf 'Discovered APKs:\n'
find "$BUILD_ROOT" -type f -name '*.apk' -print

if [ -z "$TEST_APK" ] || [ -z "$APP_APK" ]; then
  printf '%s\n' 'Required application or instrumentation APK was not found.' >&2
  exit 1
fi

exec "$REPO_ROOT/scripts/run-tdlib-device-smoke-test.sh" \
  --apk "$APP_APK" \
  --test-apk "$TEST_APK" \
  --serial "${ANDROID_SERIAL:-emulator-5554}" \
  --log-dir "$REPO_ROOT/artifacts/tdlib-smoke/$ABI"
