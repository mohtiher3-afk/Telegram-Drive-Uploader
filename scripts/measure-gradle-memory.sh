#!/usr/bin/env bash
set -euo pipefail

# CI telemetry policy: emit aggregate process-memory peaks only. Do not record
# command arguments, environment variables, source paths, or process lists.

if [[ $# -lt 2 ]]; then
  echo "Usage: $0 <safe-label> <command> [args...]" >&2
  exit 64
fi

label="$1"
shift

if [[ ! "$label" =~ ^[a-z0-9-]+$ ]]; then
  echo "Telemetry label must contain lowercase letters, digits, or hyphens only" >&2
  exit 64
fi

telemetry_dir="${MEMORY_TELEMETRY_DIR:-build/ci-memory}"
sample_interval_seconds="${MEMORY_TELEMETRY_INTERVAL_SECONDS:-2}"
mkdir -p "$telemetry_dir"
report_path="$telemetry_dir/${label}.txt"

collect_process_tree() {
  local -a frontier=("$1")
  local -a next=()
  local -a discovered=()
  local parent child

  while [[ ${#frontier[@]} -gt 0 ]]; do
    next=()
    for parent in "${frontier[@]}"; do
      [[ -d "/proc/$parent" ]] || continue
      discovered+=("$parent")
      while IFS= read -r child; do
        [[ -n "$child" ]] && next+=("$child")
      done < <(pgrep -P "$parent" 2>/dev/null || true)
    done
    frontier=("${next[@]}")
  done

  printf '%s\n' "${discovered[@]}" | sort -nu
}

read_java_heap_used_kib() {
  local pid="$1"
  local process_name heap_used
  process_name="$(ps -o comm= -p "$pid" 2>/dev/null | tr -d '[:space:]')"
  [[ "$process_name" == java* ]] || return 0

  heap_used="$(
    jcmd "$pid" GC.heap_info 2>/dev/null |
      awk '/heap[[:space:]]+total/ { for (i = 1; i <= NF; i++) if ($i == "used") { value = $(i + 1); gsub(/[^0-9]/, "", value); print value; exit } }'
  )"
  [[ "$heap_used" =~ ^[0-9]+$ ]] && printf '%s\n' "$heap_used"
}

"$@" &
command_pid="$!"
peak_tree_rss_kib=0
peak_java_heap_used_kib=0
sample_count=0

while kill -0 "$command_pid" 2>/dev/null; do
  mapfile -t process_ids < <(collect_process_tree "$command_pid")
  if [[ ${#process_ids[@]} -gt 0 ]]; then
    joined_process_ids="$(IFS=,; printf '%s' "${process_ids[*]}")"
    current_tree_rss_kib="$(ps -o rss= -p "$joined_process_ids" 2>/dev/null | awk '{ total += $1 } END { print total + 0 }')"
    (( current_tree_rss_kib > peak_tree_rss_kib )) && peak_tree_rss_kib="$current_tree_rss_kib"

    for process_id in "${process_ids[@]}"; do
      current_heap_used_kib="$(read_java_heap_used_kib "$process_id" || true)"
      [[ "$current_heap_used_kib" =~ ^[0-9]+$ ]] || continue
      (( current_heap_used_kib > peak_java_heap_used_kib )) && peak_java_heap_used_kib="$current_heap_used_kib"
    done
    ((sample_count += 1))
  fi
  sleep "$sample_interval_seconds"
done

set +e
wait "$command_pid"
command_exit_code="$?"
set -e

{
  printf 'schema_version=1\n'
  printf 'label=%s\n' "$label"
  printf 'exit_code=%s\n' "$command_exit_code"
  printf 'sample_interval_seconds=%s\n' "$sample_interval_seconds"
  printf 'sample_count=%s\n' "$sample_count"
  printf 'peak_process_tree_rss_kib=%s\n' "$peak_tree_rss_kib"
  printf 'peak_java_heap_used_kib=%s\n' "$peak_java_heap_used_kib"
} > "$report_path"

cat "$report_path"
exit "$command_exit_code"
