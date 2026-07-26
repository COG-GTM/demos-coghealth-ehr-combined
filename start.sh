#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_DIR="$ROOT_DIR/demos-coghealth-ehr-api"
WEB_DIR="$ROOT_DIR/demos-coghealth-ehr-web"

PORTS=(3000 8080 5433 6380 5673)

echo "Clearing ports ${PORTS[*]} of existing processes/containers..."
for port in "${PORTS[@]}"; do
  # Remove any docker container publishing this port
  containers="$(docker ps -q --filter "publish=$port")"
  if [ -n "$containers" ]; then
    echo "  Removing docker container(s) on port $port: $(docker ps --format '{{.Names}}' --filter "publish=$port" | tr '\n' ' ')"
    docker rm -f $containers >/dev/null
  fi
  # Kill any remaining host processes listening on this port
  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -n "$pids" ]; then
    echo "  Killing process(es) on port $port: $pids"
    kill -9 $pids 2>/dev/null || true
  fi
done

echo "Starting CogHealth Docker services..."
docker-compose -f "$API_DIR/docker-compose.yml" up -d postgres redis rabbitmq

echo "Starting CogHealth backend on http://localhost:8080/api ..."
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

echo "Starting CogHealth frontend on http://localhost:3000 ..."
nohup bash -lc "
  cd \"$WEB_DIR\"
  npm run dev -- --host 0.0.0.0 --port 3000
" > /tmp/coghealth-web.log 2>&1 &

echo
echo "Started services. Useful URLs:"
echo "  Frontend: http://localhost:3000/"
echo "  Backend:  http://localhost:8080/api"
echo "  Health:   http://localhost:8080/api/actuator/health"
echo
echo "Logs:"
echo "  tail -f /tmp/coghealth-web.log"
echo "  tail -f /tmp/coghealth-api.log"
