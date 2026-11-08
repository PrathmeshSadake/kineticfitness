#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/.run-logs"

if [[ -d "$LOG_DIR" ]]; then
  for pidfile in "$LOG_DIR"/*.pid; do
    [[ -f "$pidfile" ]] || continue
    pid="$(cat "$pidfile")"
    name="$(basename "$pidfile" .pid)"
    if kill -0 "$pid" 2>/dev/null; then
      echo "Stopping $name ($pid)"
      kill "$pid" 2>/dev/null || true
    fi
    rm -f "$pidfile"
  done
fi

echo "Done."
