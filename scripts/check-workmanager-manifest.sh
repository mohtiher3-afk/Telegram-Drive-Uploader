#!/usr/bin/env bash
set -euo pipefail

manifest="app/src/main/AndroidManifest.xml"
application="app/src/main/java/com/telegramdrive/uploader/TelegramDriveApp.kt"

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

[[ -f "$manifest" ]] || fail "Missing $manifest"
[[ -f "$application" ]] || fail "Missing $application"

grep -Fq 'android:name="androidx.startup.InitializationProvider"' "$manifest" \
  || fail "AndroidX Startup provider removal is missing"
grep -Fq 'android:authorities="${applicationId}.androidx-startup"' "$manifest" \
  || fail "AndroidX Startup provider authority is missing"
grep -Fq 'android:name="androidx.work.WorkManagerInitializer"' "$manifest" \
  || fail "Nested WorkManager initializer removal is missing"
grep -Fq 'tools:node="remove"' "$manifest" \
  || fail "WorkManager initializer removal directive is missing"
grep -Fq 'class TelegramDriveApp : Application(), Configuration.Provider' "$application" \
  || fail "Application does not provide WorkManager Configuration"
grep -Fq 'setWorkerFactory(workerFactory)' "$application" \
  || fail "Hilt worker factory is not connected to WorkManager"

echo "STATUS: WORKMANAGER_MANIFEST=PASS"
