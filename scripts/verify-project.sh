#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

./scripts/check-repository-security.sh
./scripts/check-resource-integrity.sh
./scripts/check-workmanager-manifest.sh
./scripts/check-tdlib-artifacts.sh

if [[ -x ./gradlew ]]; then
  GRADLE=(./gradlew)
elif command -v gradle >/dev/null 2>&1; then
  GRADLE=(gradle)
else
  echo 'ENVIRONMENT FAILURE: Gradle wrapper or gradle executable is required for compile/test/lint/build checks.' >&2
  exit 2
fi

"${GRADLE[@]}" --no-daemon --max-workers=2 :app:testDebugUnitTest
"${GRADLE[@]}" --no-daemon --max-workers=2 :app:lintVitalRelease
"${GRADLE[@]}" --no-daemon --max-workers=1 :app:assembleDebug
