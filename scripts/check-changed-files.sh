#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

if [[ ${1:-} == "--base" ]]; then
  [[ -n ${2:-} ]] || { echo "Usage: $0 [--base <ref>]" >&2; exit 2; }
  BASE_REF=$2
else
  BASE_REF=$(git symbolic-ref --quiet --short refs/remotes/origin/HEAD 2>/dev/null || true)
  if [[ -z "$BASE_REF" ]]; then
    DEFAULT_BRANCH=$(git config --get init.defaultBranch || true)
    DEFAULT_BRANCH=${DEFAULT_BRANCH:-main}
    BASE_REF="origin/$DEFAULT_BRANCH"
  fi
fi

if git rev-parse --verify --quiet "$BASE_REF" >/dev/null; then
  echo "BASE_REF=$BASE_REF"
  echo "--- committed changes relative to base ---"
  git diff --name-status "$BASE_REF...HEAD"
else
  echo "BASE_REF=$BASE_REF (not available locally; reporting local changes only)"
fi

echo "--- staged changes ---"
git diff --cached --name-status
echo "--- unstaged changes ---"
git diff --name-status
