#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Keep classpath config copy in sync with the editable config-repo/
cp "$ROOT"/config-repo/*.properties "$ROOT/configServer/src/main/resources/config-repo/"

for m in eureka configServer userService activityService aiService gateway; do
  echo "Packaging $m..."
  (cd "$ROOT/$m" && mvn -q -DskipTests package)
done

echo "All modules packaged."
