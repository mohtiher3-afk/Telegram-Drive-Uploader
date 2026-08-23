#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

manifest="app/src/main/AndroidManifest.xml"
en_strings="app/src/main/res/values/strings.xml"
ar_strings="app/src/main/res/values-ar/strings.xml"

[[ -f "$manifest" ]] || fail "Missing $manifest"
[[ -f "$en_strings" ]] || fail "Missing $en_strings"
[[ -f "$ar_strings" ]] || fail "Missing $ar_strings"

extract_ids() {
  grep -o 'name="[^"]*"' "$1" | sort
}

if [[ "$(extract_ids "$en_strings")" != "$(extract_ids "$ar_strings")" ]]; then
  fail "English and Arabic string resource IDs differ"
fi

if [[ "$(grep -o 'name="[^"]*"' "$en_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate English string resource ID"
fi

if [[ "$(grep -o 'name="[^"]*"' "$ar_strings" | sort | uniq -d)" != "" ]]; then
  fail "Duplicate Arabic string resource ID"
fi

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

echo "STATUS: RESOURCE_INTEGRITY=PASS"
