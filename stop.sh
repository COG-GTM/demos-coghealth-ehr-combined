#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$ROOT_DIR/demos-coghealth-ehr-api"

echo "Stopping frontend on port 3000..."
if lsof -tiTCP:3000 -sTCP:LISTEN >/tmp/coghealth-web.pid 2>/dev/null && [ -s /tmp/coghealth-web.pid ]; then
  xargs kill < /tmp/coghealth-web.pid
else
  echo "No frontend process found on port 3000."
fi

echo "Stopping backend on port 8080..."
if lsof -tiTCP:8080 -sTCP:LISTEN >/tmp/coghealth-api.pid 2>/dev/null && [ -s /tmp/coghealth-api.pid ]; then
  xargs kill < /tmp/coghealth-api.pid
else
  echo "No backend process found on port 8080."
fi

echo "Stopping CogHealth Docker services..."
docker-compose -f "$API_DIR/docker-compose.yml" down

echo "Stopped CogHealth services."
