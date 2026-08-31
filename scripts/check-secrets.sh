#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$ROOT_DIR"

# Keep one authoritative implementation for tracked secret detection.
exec "$ROOT_DIR/scripts/check-repository-security.sh"
