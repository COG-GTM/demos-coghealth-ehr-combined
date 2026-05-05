# CogHealth EHR

Electronic Health Record System - Demo Application

## Quick Start (Neon Cloud DB — no Docker needed)

```bash
# 1. Clone all repos
git clone git@github.com:COG-GTM/demos-coghealth-ehr-data.git data
git clone git@github.com:COG-GTM/demos-coghealth-ehr-api.git api
git clone git@github.com:COG-GTM/demos-coghealth-ehr-web.git web

# 2. Start API (Java 11 required)
cd api
export JAVA_HOME=/opt/homebrew/opt/openjdk@11
export PATH="$JAVA_HOME/bin:$PATH"
export NEON_DB_URL="jdbc:postgresql://<neon-host>/neondb?sslmode=require"
export NEON_DB_USERNAME="<username>"
export NEON_DB_PASSWORD="<password>"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
cd ..

# 3. Start Web (Node.js 20.19+ or 22.12+ required)
cd web
npm install
npm run dev
```

Open http://localhost:5173

Get Neon credentials from your team lead or `.env` file.

### Quick Start (Local Docker)

```bash
# 1. Start infrastructure (Docker required)
cd data
docker-compose up -d
docker exec -i coghealth-postgres psql -U coghealth coghealth < seed.sql
cd ..

# 2. Start API
cd api
mvn spring-boot:run
cd ..

# 3. Start Web
cd web
npm install
npm run dev
```

## Repositories

| Repo | Description | Port |
|------|-------------|------|
| [data](https://github.com/COG-GTM/demos-coghealth-ehr-data) (this repo) | Docker infrastructure (PostgreSQL, Redis, Keycloak) | 5432, 6379, 8180 |
| [api](https://github.com/COG-GTM/demos-coghealth-ehr-api) | Spring Boot REST API | 8080 |
| [web](https://github.com/COG-GTM/demos-coghealth-ehr-web) | React frontend | 5173 |

## Prerequisites

**For Neon (cloud DB):**
- Java 11 (`brew install openjdk@11`)
- Node.js 20.19+ or 22.12+ (`nvm install 22`)
- Maven (`brew install maven`)

**For local Docker:**
- All of the above, plus Docker Desktop

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   React     │────▶│  Spring     │────▶│ PostgreSQL  │
│   :5173     │     │  Boot :8080 │     │ (Neon/local)│
└─────────────┘     └─────────────┘     └─────────────┘
```

## Demo Users

All users have password: `demo123`

| Username | Role |
|----------|------|
| dr.anderson | Physician |
| nurse.johnson | Nurse |
| ma.smith | Medical Assistant |
| frontdesk.wilson | Front Desk |
| billing.garcia | Billing |

## URLs

| Service | URL |
|---------|-----|
| Web App | http://localhost:5173 |
| API Swagger | http://localhost:8080/api/swagger-ui/index.html |
| Keycloak | http://localhost:8180 (admin/admin) |
| RabbitMQ | http://localhost:15672 (coghealth/coghealth_dev_2024) |

## Infrastructure Services

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 5432 | coghealth / coghealth_dev_2024 |
| Redis | 6379 | - |
| Keycloak | 8180 | admin / admin |
| RabbitMQ | 5672, 15672 | coghealth / coghealth_dev_2024 |
| Elasticsearch | 9200 | - |
| Kibana | 5601 | - |

## Files in This Repo

| File | Description |
|------|-------------|
| docker-compose.yml | Infrastructure services |
| seed.sql | Database seed data |
| docker/keycloak/ | Keycloak realm config |

## Database Options

**Neon (cloud — recommended):** No Docker needed. Uses a shared cloud PostgreSQL instance with pre-seeded data. See Quick Start above.

**Local (Docker):** Uses Docker PostgreSQL with seed data. Requires Docker Desktop. See Local Docker section above.

## Commands

```bash
docker-compose up -d      # Start infrastructure
docker-compose down       # Stop infrastructure
docker-compose logs -f    # View logs
```

## License

Demo Application - Cognition AI
