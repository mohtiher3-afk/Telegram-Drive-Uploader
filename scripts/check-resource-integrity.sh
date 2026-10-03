#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

manifest="app/src/main/AndroidManifest.xml"
en_strings="app/src/main/res/values/strings.xml"
ar_strings="app/src/main/res/values-ar/strings.xml"
feature_en_strings="feature/src/main/res/values/strings.xml"
feature_ar_strings="feature/src/main/res/values-ar/strings.xml"

[[ -f "$manifest" ]] || fail "Missing $manifest"
[[ -f "$en_strings" ]] || fail "Missing $en_strings"
[[ -f "$ar_strings" ]] || fail "Missing $ar_strings"
[[ -f "$feature_en_strings" ]] || fail "Missing $feature_en_strings"
[[ -f "$feature_ar_strings" ]] || fail "Missing $feature_ar_strings"

extract_ids() {
  grep -o 'name="[^"]*"' "$1" | sort
}

if [[ "$(extract_ids "$en_strings")" != "$(extract_ids "$ar_strings")" ]]; then
  fail "English and Arabic string resource IDs differ"
fi

if [[ "$(extract_ids "$feature_en_strings")" != "$(extract_ids "$feature_ar_strings")" ]]; then
  fail "Feature English and Arabic string resource IDs differ"
fi

if [[ "$(grep -o 'name="[^"]*"' "$en_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate English string resource ID"
fi

if [[ "$(grep -o 'name="[^"]*"' "$ar_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate Arabic string resource ID"
fi

if [[ "$(grep -o 'name="[^"]*"' "$feature_en_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate feature English string resource ID"
fi

if [[ "$(grep -o 'name="[^"]*"' "$feature_ar_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate feature Arabic string resource ID"
fi

check_no_mojibake() {
  local file=$1
  local label=$2

  # UTF-8 Arabic uses D8/D9 lead bytes. Box-drawing, Greek mojibake fragments,
  # and U+FFFD indicate a prior double-decoding corruption.
  if LC_ALL=C.UTF-8 grep -nP '[\x{0391}-\x{03C9}\x{2500}-\x{257F}\x{FFFD}]' "$file"; then
    fail "$label contains mojibake marker characters; restore it as UTF-8"
  fi
}

check_no_mojibake "$en_strings" "English app strings"
check_no_mojibake "$ar_strings" "Arabic app strings"
check_no_mojibake "$feature_en_strings" "English feature strings"
check_no_mojibake "$feature_ar_strings" "Arabic feature strings"

grep -Fq 'android:supportsRtl="true"' "$manifest" \
  || fail "RTL support is not enabled in the manifest"
grep -Fq 'android:icon="@mipmap/ic_launcher"' "$manifest" \
  || fail "Manifest launcher icon reference is missing"
grep -Fq 'android:roundIcon="@mipmap/ic_launcher_round"' "$manifest" \
  || fail "Manifest round launcher icon reference is missing"
grep -Fq 'android:dataExtractionRules="@xml/data_extraction_rules"' "$manifest" \
  || fail "Data extraction rules reference is missing"
grep -Fq 'android:fullBackupContent="@xml/backup_rules"' "$manifest" \
  || fail "Backup rules reference is missing"

for file in \
  app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml; do
  [[ -f "$file" ]] || fail "Missing adaptive icon resource $file"
done

grep -Fq '@drawable/ic_launcher_background' app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  || fail "Adaptive icon background reference is missing"
grep -Fq '@drawable/ic_launcher_foreground' app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  || fail "Adaptive icon foreground reference is missing"
grep -Fq '@drawable/ic_launcher_monochrome' app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml \
  || fail "Adaptive icon monochrome reference is missing"

grep -RIn --include='*.kt' 'getIdentifier(' app/src/main \
  && fail "Dynamic resource lookup requires explicit shrinker review" || true

mojibake_python=""
if command -v python >/dev/null 2>&1 && python -c 'import sys' >/dev/null 2>&1; then
  mojibake_python="python"
elif command -v python3 >/dev/null 2>&1 && python3 -c 'import sys' >/dev/null 2>&1; then
  mojibake_python="python3"
fi
[[ -n "$mojibake_python" ]] || fail "Python is required for the repository-wide mojibake check"
"$mojibake_python" scripts/check-mojibake.py \
  || fail "Mojibake detected; run $mojibake_python scripts/check-mojibake.py --fix"

echo "STATUS: RESOURCE_INTEGRITY=PASS"
