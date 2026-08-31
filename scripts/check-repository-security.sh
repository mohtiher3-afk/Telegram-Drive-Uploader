#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "SECURITY FAILURE: $1" >&2
  exit 1
}

# Scan tracked text files only. Never print matching content.
mapfile -d '' tracked_files < <(git ls-files -z -- ':!docs/**' ':!.env.example' ':!.github/workflows/**')
if ((${#tracked_files[@]} == 0)); then
  echo "STATUS: SECURITY_SCAN=PASS"
  exit 0
fi

if printf '%s\0' "${tracked_files[@]}" | xargs -0 grep -IlE -- '-----BEGIN (RSA|OPENSSH|EC|DSA|PRIVATE) KEY-----' >/dev/null 2>&1; then
  fail 'private-key material detected in tracked source files'
fi

if printf '%s\0' "${tracked_files[@]}" | xargs -0 grep -IlE 'Authorization:[[:space:]]*Bearer[[:space:]]+[A-Za-z0-9._~-]{20,}' >/dev/null 2>&1; then
  fail 'bearer-token material detected in tracked source files'
fi

if printf '%s\0' "${tracked_files[@]}" | xargs -0 grep -IlE '[0-9]{8,12}:[A-Za-z0-9_-]{30,}' >/dev/null 2>&1; then
  fail 'Telegram bot-token-shaped material detected in tracked source files'
fi

echo "STATUS: SECURITY_SCAN=PASS"
