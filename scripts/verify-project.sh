#!/usr/bin/env bash
set -uo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

MODE=${1:-FULL}
case "$MODE" in
  QUICK|FULL|RELEASE|CLEAN) ;;
  *) echo "Usage: $0 [QUICK|FULL|RELEASE|CLEAN]" >&2; exit 2 ;;
esac

REPORT_DIR="$ROOT_DIR/build/reports/verification"
REPORT_FILE="$REPORT_DIR/verification-summary.txt"
mkdir -p "$REPORT_DIR"
: > "$REPORT_FILE"

declare -A RESULTS
FAILED=0
STEP=0

record() {
  printf '%-12s %s\n' "$1" "$2" >> "$REPORT_FILE"
}

run_check() {
  local key=$1 title=$2 log rc
  shift 2
  STEP=$((STEP + 1))
  printf '\n[%s/9] %s\n' "$STEP" "$title"
  log=$(mktemp)
  "$@" >"$log" 2>&1
  rc=$?
  if [[ $rc -eq 0 ]]; then
    RESULTS["$key"]="PASS"
    record "$key" "PASS"
    tail -n 3 "$log" || true
  else
    RESULTS["$key"]="FAIL"
    FAILED=1
    record "$key" "FAIL"
    echo "Failure output:" >&2
    tail -n 80 "$log" >&2 || true
  fi
  rm -f "$log"
  return "$rc"
}

repository_sanity() {
  git diff --check
  if git grep -nE '^(<<<<<<<|=======|>>>>>>>)' -- ':!docs/**' >/dev/null 2>&1; then
    echo 'merge conflict markers found' >&2
    git grep -nE '^(<<<<<<<|=======|>>>>>>>)' -- ':!docs/**' >&2 || true
    return 1
  fi
  tracked_generated=$(git ls-files | grep -E '(^|/)(build|out)/|\.(apk|aab|jks|keystore)$' || true)
  if [[ -n "$tracked_generated" ]]; then
    echo 'suspicious generated artifacts are tracked:' >&2
    printf '%s\n' "$tracked_generated" >&2
    return 1
  fi
  "$ROOT_DIR/scripts/check-changed-files.sh" >/dev/null
}

gradle_help() {
  ./gradlew --no-daemon --max-workers=2 :app:help
}

gradle_compile() {
  ./gradlew --no-daemon --max-workers=2 :app:compileDebugKotlin
}

gradle_tests() {
  ./gradlew --no-daemon --max-workers=2 :app:testDebugUnitTest
}

gradle_lint() {
  ./gradlew --no-daemon --max-workers=2 :app:lintVitalRelease
}

gradle_builds() {
  ./gradlew --no-daemon --max-workers=1 :app:assembleDebug
  if [[ "$MODE" == RELEASE ]]; then
    ./gradlew --no-daemon --max-workers=1 :app:assembleRelease
  fi
}

if [[ "$MODE" == CLEAN ]]; then
  echo '[clean] Clearing project build outputs only'
  ./gradlew --no-daemon --max-workers=1 clean
  MODE=FULL
fi

run_check repository 'Repository' repository_sanity || true
run_check tdlib 'TDLib' "$ROOT_DIR/scripts/check-tdlib-artifacts.sh" || true
run_check gradle 'Gradle' gradle_help || true

if [[ "$MODE" == QUICK ]]; then
  run_check compile 'Compile' gradle_compile || true
  run_check tests 'Tests' gradle_tests || true
else
  run_check compile 'Compile' gradle_compile || true
  run_check tests 'Tests' gradle_tests || true
  run_check lint 'Lint' gradle_lint || true
  run_check build 'Build' gradle_builds || true
fi

security_checks() {
  "$ROOT_DIR/scripts/check-secrets.sh"
  "$ROOT_DIR/scripts/check-production-code.sh"
  "$ROOT_DIR/scripts/check-resource-integrity.sh"
  "$ROOT_DIR/scripts/check-workmanager-manifest.sh"
}
run_check security 'Security' security_checks || true

# Keep the result file useful even after a failed command; never include command
# output, credentials, or private paths beyond the repository-relative status.
printf '\n[9/9] Final Result\n'
{
  echo "MODE=$MODE"
  echo "Repository=${RESULTS[repository]:-NOT RUN}"
  echo "TDLib=${RESULTS[tdlib]:-NOT RUN}"
  echo "Gradle=${RESULTS[gradle]:-NOT RUN}"
  echo "Compile=${RESULTS[compile]:-NOT RUN}"
  echo "Tests=${RESULTS[tests]:-NOT RUN}"
  echo "Lint=${RESULTS[lint]:-NOT RUN}"
  echo "Build=${RESULTS[build]:-NOT RUN}"
  echo "Security=${RESULTS[security]:-NOT RUN}"
  echo "ReleaseBuild=$([[ "$MODE" == RELEASE ]] && echo "included" || echo "NOT RUN")"
} | tee -a "$REPORT_FILE"

if [[ $FAILED -eq 0 ]]; then
  echo 'VERIFICATION PASSED' | tee -a "$REPORT_FILE"
  exit 0
fi

echo 'VERIFICATION FAILED' | tee -a "$REPORT_FILE"
exit 1
