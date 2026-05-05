#!/bin/bash

# CogHealth EHR - Docker Infrastructure Startup Script

set -e

echo "=========================================="
echo "  CogHealth EHR Infrastructure Setup"
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

echo "✅ Docker is running"

# Navigate to script directory
cd "$(dirname "$0")"

# Stop any existing containers
echo ""
echo "Stopping any existing containers..."
docker-compose down 2>/dev/null || true

# Start infrastructure
echo ""
echo "Starting infrastructure services..."
echo "This may take a few minutes on first run (downloading images)..."
echo ""

docker-compose up -d

# Wait for services to be healthy
echo ""
echo "Waiting for services to be ready..."

# Wait for PostgreSQL
echo -n "  PostgreSQL: "
until docker exec coghealth-postgres pg_isready -U coghealth -d coghealth > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ✅"

# Wait for Redis
echo -n "  Redis: "
until docker exec coghealth-redis redis-cli ping > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo " ✅"

# Wait for Keycloak
echo -n "  Keycloak: "
until curl -s http://localhost:8180/health/ready > /dev/null 2>&1; do
    echo -n "."
    sleep 3
done
echo " ✅"

# Wait for RabbitMQ
echo -n "  RabbitMQ: "
until curl -s -u coghealth:coghealth_dev_2024 http://localhost:15672/api/overview > /dev/null 2>&1; do
    echo -n "."
    sleep 3
done
echo " ✅"

# Wait for Elasticsearch
echo -n "  Elasticsearch: "
until curl -s http://localhost:9200/_cluster/health > /dev/null 2>&1; do
    echo -n "."
    sleep 3
done
echo " ✅"

# Wait for HAPI FHIR
echo -n "  HAPI FHIR: "
until curl -s http://localhost:8090/fhir/metadata > /dev/null 2>&1; do
    echo -n "."
    sleep 3
done
echo " ✅"

echo ""
echo "=========================================="
echo "  All services are running!"
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  • PostgreSQL:    localhost:5432 (coghealth/coghealth_dev_2024)"
echo "  • Redis:         localhost:6379"
echo "  • Keycloak:      http://localhost:8180 (admin/admin)"
echo "  • RabbitMQ:      http://localhost:15672 (coghealth/coghealth_dev_2024)"
echo "  • Elasticsearch: http://localhost:9200"
echo "  • Kibana:        http://localhost:5601"
echo "  • HAPI FHIR:     http://localhost:8090/fhir"
echo ""
echo "Demo Users (password: demo123):"
echo "  • dr.anderson      - Physician"
echo "  • nurse.johnson    - Nurse"
echo "  • ma.smith         - Medical Assistant"
echo "  • frontdesk.wilson - Front Desk"
echo "  • billing.garcia   - Billing"
echo "  • compliance.lee   - Compliance Officer"
echo "  • admin (password: admin123) - System Admin"
echo ""
echo "To stop: docker-compose down"
echo "To view logs: docker-compose logs -f [service]"
echo ""
