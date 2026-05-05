#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$ROOT_DIR/demos-coghealth-ehr-api"
WEB_DIR="$ROOT_DIR/demos-coghealth-ehr-web"

echo "Starting CogHealth Docker services..."
docker-compose -f "$API_DIR/docker-compose.yml" up -d postgres redis rabbitmq

echo "Starting CogHealth backend on http://localhost:8080/api ..."
if lsof -tiTCP:8080 -sTCP:LISTEN >/tmp/coghealth-api.pid 2>/dev/null && [ -s /tmp/coghealth-api.pid ]; then
  echo "Backend already appears to be running on port 8080."
else
  nohup bash -lc "
    export JAVA_HOME=/opt/homebrew/opt/openjdk@11
    export PATH=\"\$JAVA_HOME/bin:\$PATH\"
    export DB_URL=\"jdbc:postgresql://localhost:5433/coghealth\"
    export DB_USERNAME=\"coghealth\"
    export DB_PASSWORD=\"coghealth_dev_2024\"
    export SPRING_REDIS_PORT=6380
    export SPRING_RABBITMQ_PORT=5673
    /opt/homebrew/bin/mvn -f \"$API_DIR/pom.xml\" spring-boot:run
  " > /tmp/coghealth-api.log 2>&1 &
fi

echo "Starting CogHealth frontend on http://localhost:3000 ..."
if lsof -tiTCP:3000 -sTCP:LISTEN >/tmp/coghealth-web.pid 2>/dev/null && [ -s /tmp/coghealth-web.pid ]; then
  echo "Frontend already appears to be running on port 3000."
else
  nohup bash -lc "
    cd \"$WEB_DIR\"
    npm run dev -- --host 0.0.0.0 --port 3000
  " > /tmp/coghealth-web.log 2>&1 &
fi

echo
echo "Started services. Useful URLs:"
echo "  Frontend: http://localhost:3000/"
echo "  Backend:  http://localhost:8080/api"
echo "  Health:   http://localhost:8080/api/actuator/health"
echo
echo "Logs:"
echo "  tail -f /tmp/coghealth-web.log"
echo "  tail -f /tmp/coghealth-api.log"
