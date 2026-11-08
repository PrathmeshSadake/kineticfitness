#!/usr/bin/env bash
# Start the full Kinetic Fitness stack locally (JARs must already be built).
# Usage:
#   cp .env.example local.env   # then fill secrets
#   set -a && source local.env && set +a
#   ./scripts/package-all.sh
#   ./scripts/start-all.sh

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/.run-logs"
mkdir -p "$LOG_DIR"

if [[ ! -f "$ROOT/local.env" ]]; then
  echo "Missing local.env. Copy .env.example to local.env and fill in secrets."
  exit 1
fi

set -a
# shellcheck disable=SC1091
source "$ROOT/local.env"
set +a

start_jar() {
  local name="$1"
  local jar="$2"
  if [[ ! -f "$jar" ]]; then
    echo "Missing $jar — run ./scripts/package-all.sh first"
    exit 1
  fi
  echo "Starting $name..."
  nohup java -jar "$jar" > "$LOG_DIR/${name}.log" 2>&1 &
  echo $! > "$LOG_DIR/${name}.pid"
}

start_jar eureka "$ROOT/eureka/target/eureka-0.0.1-SNAPSHOT.jar"
sleep 4
start_jar configServer "$ROOT/configServer/target/configServer-0.0.1-SNAPSHOT.jar"
sleep 4
start_jar userService "$ROOT/userService/target/userService-0.0.1-SNAPSHOT.jar"
start_jar activityService "$ROOT/activityService/target/activityService-0.0.1-SNAPSHOT.jar"
start_jar aiService "$ROOT/aiService/target/aiService-0.0.1-SNAPSHOT.jar"
sleep 6
start_jar gateway "$ROOT/gateway/target/gateway-0.0.1-SNAPSHOT.jar"

echo "Backend started. Logs: $LOG_DIR"
echo "Frontend: cd frontend && npm install && npm run dev"
echo "Gateway:  http://localhost:8080"
echo "UI:       http://localhost:5173"
