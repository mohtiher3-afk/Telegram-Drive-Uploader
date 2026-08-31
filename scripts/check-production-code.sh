#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

# This is a detector, not an auto-fixer. Documentation and tests are classified but
# do not fail the gate. A NotImplementedException in shipped app code is blocking.
status=0
matches=$(grep -RInE --exclude-dir=.git --exclude-dir=build --exclude='*.md' --exclude='*.txt' \
  '\\b(NotImplementedException|TODO|FIXME|fake|mock|dummy|debug-only)\\b' app/src/main .github 2>/dev/null || true)

if [[ -n "$matches" ]]; then
  echo "Production-code findings (values shown are marker names and locations only):"
  while IFS= read -r line; do
    file=${line%%:*}
    rest=${line#*:}
    line_number=${rest%%:*}
    message=${rest#*:}
    marker=$(printf '%s\n' "$message" | grep -oE '\b(NotImplementedException|TODO|FIXME|fake|mock|dummy|debug-only)\b' | head -1 || true)
    printf '  %s:%s [%s]\n' "$file" "$line_number" "${marker:-classified finding}"
    if [[ "$marker" == "NotImplementedException" && "$file" == app/src/main/* ]]; then
      status=1
    fi
  done <<< "$matches"
fi

if [[ $status -ne 0 ]]; then
  echo "STATUS: PRODUCTION_CODE=FAIL" >&2
  exit 1
fi

echo "STATUS: PRODUCTION_CODE=PASS"
