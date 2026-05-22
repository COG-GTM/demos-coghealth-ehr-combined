# CogHealth EHR

Electronic Health Record System — Demo Application & Workshop Environment

> This is a **monorepo** version of CogHealth EHR, combining the frontend, API, and data components into a single repository for workshop and demonstration purposes.

## Quick Start

The `start.sh` script orchestrates all services:

```bash
# Start everything (Docker infrastructure + API + Web)
./start.sh

# Stop everything
./stop.sh
```

This will start:
- PostgreSQL, Redis, RabbitMQ (via Docker)
- Spring Boot API on `http://localhost:8080/api`
- React frontend on `http://localhost:3000`

### Manual Quick Start

If you prefer to start services individually:

```bash
# 1. Start infrastructure (Docker required)
cd demos-coghealth-ehr-data
docker-compose up -d
docker exec -i coghealth-postgres psql -U coghealth coghealth < seed.sql
cd ..

# 2. Start API (Java 11 required)
cd demos-coghealth-ehr-api
export JAVA_HOME=/opt/homebrew/opt/openjdk@11
export PATH="$JAVA_HOME/bin:$PATH"
mvn spring-boot:run
cd ..

# 3. Start Web (Node.js 20.19+ or 22.12+ required)
cd demos-coghealth-ehr-web
npm install
npm run dev -- --host 0.0.0.0 --port 3000
```

## Prerequisites

- **Java 11** (required — later versions are incompatible)
- **Node.js 20.19+ or 22.12+** (required by Vite 7)
- **Maven**
- **Docker Desktop** (for local infrastructure)

## Repository Structure

```
workshop/
├── demos-coghealth-ehr-api/      # Spring Boot REST API
├── demos-coghealth-ehr-web/      # React frontend
├── demos-coghealth-ehr-data/     # Docker infrastructure (PostgreSQL, Redis, RabbitMQ)
├── start.sh                      # Orchestration script to start all services
├── stop.sh                       # Script to stop all services
└── AGENTS.md                     # Agent instructions and patterns
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
| Web App | http://localhost:3000 |
| API Swagger | http://localhost:8080/api/swagger-ui/index.html |
| API Health Check | http://localhost:8080/api/actuator/health |
| RabbitMQ Management | http://localhost:15672 (coghealth/coghealth_dev_2024) |
| Kibana (Audit Logs) | http://localhost:5601 |
| Elasticsearch | http://localhost:9200 |

## Infrastructure Services

| Service | Port | Credentials |
|---------|------|-------------|
| PostgreSQL | 5433 | coghealth / coghealth_dev_2024 |
| Redis | 6380 | - |
| RabbitMQ | 5673, 15672 | coghealth / coghealth_dev_2024 |
| Elasticsearch | 9200 | - |
| Kibana | 5601 | - |

---

## Component Details

### Backend: `demos-coghealth-ehr-api`

Spring Boot REST API providing healthcare data services with JWT authentication, HIPAA audit logging, and FHIR interoperability support.

#### Tech Stack
- **Java 11** (required — later versions incompatible)
- **Spring Boot 2.7.18**
- **Spring Data JPA** (Hibernate ORM)
- **PostgreSQL** (with Flyway migrations)
- **Spring Security** with JWT (JJWT 0.11.5)
- **Spring Data Redis** (caching, sessions)
- **Spring AOP** (audit logging aspect)
- **HAPI FHIR 2.3** (HL7 FHIR R4 support)
- **MapStruct 1.5.5** (entity/DTO mapping)
- **Lombok** (boilerplate reduction)
- **SpringDoc OpenAPI 1.7.0** (Swagger UI)

#### Domain Model

**Core Entities:**
- `patient/` — Patient records, identifiers, emergency contacts
- `provider/` — Healthcare providers (NPI, credentials, specialties)
- `encounter/` — Clinical encounters (visits, appointments)
- `medication/` — Medications, prescriptions, dispensing
- `clinical/` — Clinical observations, vitals, lab results
- `insurance/` — Insurance plans, eligibility, coverage
- `order/` — Orders (lab, imaging, referrals)
- `chronic/` — Chronic conditions, medication adherence, diabetes management
- `auth/` — Users, roles, permissions

#### Controllers

| Controller | Endpoints | Purpose |
|------------|-----------|---------|
| `AuthController` | `/api/v1/auth/*` | Login, token refresh, user profile |
| `PatientController` | `/api/v1/patients/*` | Patient CRUD, search, identifiers |
| `ProviderController` | `/api/v1/providers/*` | Provider CRUD, search, schedules |
| `EncounterController` | `/api/v1/encounters/*` | Encounter CRUD, vitals, clinical notes |
| `LegacyExportController` | `/api/v1/legacy/export/*` | Bulk data export (HIPAA issue: no audit) |

#### Services

| Service | Responsibility |
|---------|---------------|
| `PatientService` | Patient business logic, search, validation |
| `ProviderService` | Provider lookup, scheduling |
| `EncounterService` | Encounter management, clinical data |
| `AppointmentService` | Appointment scheduling, insurance eligibility caching |
| `InsuranceGateway` | External insurance eligibility API (circuit breaker) |
| `ProviderNotificationService` | Async provider notifications (email, SMS) |
| `ChronicMedicationService` | Chronic medication management (stub) |
| `MedicationAdherenceTracker` | PDC calculation, care gaps (stub) |
| `PharmacyIntegrationService` | NCPDP pharmacy communication (stub) |
| `MedicationNotificationService` | Medication reminders (stub) |

#### Audit Logging

HIPAA-compliant audit logging via AOP aspect:

```java
@AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient")
public PatientDTO getPatient(Long id) { ... }
```

**Components:**
- `@AuditAccess` — Annotation for marking audited methods
- `AuditAspect` — AOP aspect that intercepts annotated methods
- `PatientAccessLogger` — Logs patient data access
- `MedicationAuditLogger` — Logs medication access
- `AuditService` — Persists audit events to database
- `AuditEvent` — Audit event entity

#### FHIR Support

- `FhirPatientMapper` — Maps between internal Patient model and FHIR R4 Patient resource
- HAPI FHIR libraries for HL7 message parsing/generation

#### Legacy/Problematic Code (HIPAA Issues)

Intentionally planted compliance gaps:

| File | Issue |
|------|-------|
| `PatientService.java:85` | Logs full SSN in error messages |
| `EncounterExportService.java` | No audit logging on batch exports |
| `LegacyPatientLookup.java` | Direct JDBC bypasses audit layer |
| `InsuranceCache.java` | Caches SSN with no TTL/expiration |
| `ReportGenerator.java` | Writes PHI to temp files without cleanup |

#### Project Structure

```
src/main/java/com/medchart/ehr/
├── controller/        # REST controllers
├── domain/            # JPA entities
│   ├── auth/
│   ├── chronic/
│   ├── clinical/
│   ├── encounter/
│   ├── insurance/
│   ├── medication/
│   ├── order/
│   ├── patient/
│   └── provider/
├── dto/               # Data transfer objects
├── repository/        # JPA repositories
├── service/           # Business logic
│   └── chronic/       # Chronic medication services (stubs)
├── config/            # Security, JWT, Redis config
├── audit/             # HIPAA audit logging
├── mapper/            # Entity/DTO mappers, FHIR mappers
├── legacy/            # Legacy code with HIPAA issues
└── EhrApplication.java
```

#### Configuration Profiles

- **default** — Local PostgreSQL via Docker
- **dev** — Neon cloud PostgreSQL (shared instance with test data)

---

### Frontend: `demos-coghealth-ehr-web`

React 19 + TypeScript frontend with Tailwind CSS, providing an EHR desktop-style interface.

#### Tech Stack
- **React 19.2.0**
- **TypeScript 5.9.3**
- **Vite 7.2.4** (build tool & dev server)
- **Tailwind CSS 4.1.18** (utility-first styling)
- **React Router DOM 7.12.0** (routing)
- **TanStack Query 5.90.19** (server state management, caching)
- **Zustand 5.0.10** (client state management)
- **React Hook Form 7.71.1** (form handling)
- **Zod 4.3.5** (schema validation)
- **Lucide React 0.562.0** (icons)
- **Jest 30.2.0** (testing)
- **Puppeteer 24.35.0** (E2E testing)

#### Pages

| Page | Route | Description |
|------|-------|-------------|
| `DashboardPage` | `/` | Provider dashboard, patient summary |
| `PatientSearchPage` | `/patients/search` | Patient search with filters |
| `PatientChartPage` | `/patients/:id` | Full patient chart view |
| `SchedulePage` | `/schedule` | Appointment calendar |
| `MedicationsPage` | `/medications` | Medication management |
| `LabResultsPage` | `/labs` | Lab results viewer |
| `VitalsPage` | `/vitals` | Vitals entry and history |
| `ReportsPage` | `/reports` | Reporting interface |
| `SettingsPage` | `/settings` | User settings |

#### Components

**Patient Components:**
- `PatientBanner` — Patient header with demographics, alerts
- `PatientSearch` — Search input with autocomplete

**UI Components:**
- `Badge`, `StatusBadge` — Status indicators
- `Button` — Styled button variants
- `Card` — Content containers
- `FlagChip` — Alert/flag chips
- `Input` — Form inputs
- `LoadingOverlay` — Loading state overlay
- `Modal` — Dialog modals
- `OrderDialog` — Order entry dialog
- `PrescriptionDialog` — Prescription entry
- `PrintDialog` — Print preview dialog
- `ThemeToggle` — Light/dark mode toggle
- `Tooltip` — Hover tooltips

#### Services

| Service | Purpose |
|---------|---------|
| `api.ts` | Axios configuration, base client |
| `patientService.ts` | Patient API calls |
| `encounterService.ts` | Encounter API calls |
| `auditService.ts` | Audit log queries |

#### Types

| Type | Description |
|------|-------------|
| `patient.ts` | Patient, PatientIdentifier, Address types |
| `encounter.ts` | Encounter, Vitals, ClinicalNote types |
| `medication.ts` | Medication, Prescription types |
| `lab.ts` | LabResult, LabOrder types |
| `vitals.ts` | Vitals, BloodPressure, etc. |

#### State Management

- **Zustand stores** in `src/stores/` — Client-side state (theme, user session, UI state)
- **TanStack Query** — Server state caching, automatic refetching, optimistic updates

#### Build & Test

```bash
npm run dev      # Start dev server (port 3000)
npm run build    # Production build
npm run lint     # ESLint
npm test         # Unit tests
npm run test:e2e # E2E tests with Puppeteer
```

---

### Data: `demos-coghealth-ehr-data`

Docker infrastructure and database seed data for local development.

#### Docker Services

| Service | Image | Ports | Purpose |
|---------|-------|-------|---------|
| PostgreSQL | postgres:14-alpine | 5432 → 5433 | Primary database |
| Redis | redis:7-alpine | 6379 → 6380 | Cache, sessions |
| RabbitMQ | rabbitmq:3-management-alpine | 5672, 15672 | Message queue (HL7, notifications) |
| Elasticsearch | elasticsearch:8.11.0 | 9200 | Audit log storage |
| Kibana | kibana:8.11.0 | 5601 | Audit log UI |
| Keycloak | quay.io/keycloak/keycloak:23.0 | 8180 | SSO, RBAC, MFA (configured but not actively used) |
| HAPI FHIR | hapiproject/hapi:latest | 8090 | FHIR server (commented out) |

#### Database Schema

**Core Tables:**
- `patients` — Patient demographic records
- `patient_identifiers` — External IDs (MRN, SSN, insurance IDs)
- `emergency_contacts` — Emergency contact information
- `providers` — Healthcare provider records
- `encounters` — Clinical encounters/visits
- `medications` — Medication catalog
- `prescriptions` — Patient prescriptions
- `lab_orders` — Lab test orders
- `lab_results` — Lab test results
- `audit_events` — HIPAA audit trail

#### Seed Data

`seed.sql` contains sample data for:
- 50+ patients with varied demographics
- 10+ providers (physicians, nurses, MAs)
- Sample encounters and clinical data
- Insurance plans and coverage
- Audit log examples

#### Commands

```bash
docker-compose up -d      # Start all infrastructure
docker-compose down       # Stop all infrastructure
docker-compose logs -f    # View logs
docker-compose ps         # Check service status
```

---

## HIPAA Compliance Audit Demo

This codebase contains **intentionally planted HIPAA compliance gaps** for audit demonstration purposes. These are meant to be discovered and fixed as part of a compliance exercise.

### Known HIPAA Issues

1. **PatientService.java:85** — Logs full SSN in error messages
2. **EncounterExportService.java** — No audit logging on batch exports
3. **LegacyPatientLookup.java** — Direct JDBC bypasses audit layer
4. **InsuranceCache.java** — Caches SSN with no TTL/expiration
5. **ReportGenerator.java** — Writes PHI to temp files without cleanup
6. **15+ endpoints** — Missing access logging

### Audit Logging Pattern

```java
@AuditAccess(action = AuditAction.READ, resourceType = "Patient", description = "View patient")
public PatientDTO getPatient(Long id) { ... }
```

See the API's [AGENTS.md](demos-coghealth-ehr-api/AGENTS.md) for detailed patterns and compliance guidance.

## Chronic Medication Module

The chronic medication module is partially implemented and serves as a hands-on implementation task.

### Status

**Complete:**
- Domain objects (`ChronicCondition`, `MedicationAdherence`, `DiabetesManagement`)

**Stubs (Need Implementation):**
- `ChronicMedicationService` — Chronic medication CRUD, care plans
- `MedicationAdherenceTracker` — PDC calculation, care gap identification
- `PharmacyIntegrationService` — NCPDP pharmacy communication
- `MedicationNotificationService` — Medication reminders, adherence alerts

### Implementation Tasks

1. Create repositories for chronic domain objects
2. Implement PDC calculation in MedicationAdherenceTracker
3. Implement care gap identification logic
4. Add NCPDP integration for pharmacy communication
5. Create REST endpoints for chronic care dashboard
6. Build frontend components for chronic care management

See the API's [AGENTS.md](demos-coghealth-ehr-api/AGENTS.md) for more details.

## Architecture Patterns

### API Patterns

- **Repository Pattern** — JPA repositories abstract database access
- **DTO Pattern** — Data transfer objects separate API contracts from domain entities
- **Service Layer** — Business logic encapsulated in service classes
- **AOP Audit Logging** — Cross-cutting audit logging via Spring AOP
- **Circuit Breaker** — Resilience pattern for external API calls (InsuranceGateway)
- **Async Processing** — @Async for non-blocking operations (ProviderNotificationService)

### Frontend Patterns

- **Component Composition** — Reusable UI components with clear responsibilities
- **Server State** — TanStack Query for API caching, refetching, optimistic updates
- **Client State** — Zustand for UI state (theme, modals, selections)
- **Type Safety** — TypeScript types mirroring backend DTOs
- **Form Validation** — React Hook Form + Zod for type-safe form handling

## Troubleshooting

### Common Issues

**Port conflicts:**
```bash
# Check what's using a port
lsof -i :8080  # API
lsof -i :3000  # Web
lsof -i :5433  # PostgreSQL

# Kill the process
lsof -ti:8080 | xargs kill -9
```

**API won't start:**
- Ensure Java 11 is set: `export JAVA_HOME=/opt/homebrew/opt/openjdk@11`
- Check database connectivity: verify Docker services are running
- Review logs: `tail -f /tmp/coghealth-api.log`

**Frontend build fails:**
- Verify Node.js version: `node --version` (must be 20.19+ or 22.12+)
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Check API is accessible at `http://localhost:8080/api`

**Database connection issues:**
- Verify Docker containers: `docker-compose ps` in `demos-coghealth-ehr-data`
- Check PostgreSQL logs: `docker logs coghealth-postgres`
- Re-run seed data: `docker exec -i coghealth-postgres psql -U coghealth coghealth < seed.sql`

## Component Documentation

For detailed information about each component, see their respective READMEs:

- [API README](demos-coghealth-ehr-api/README.md)
- [Web README](demos-coghealth-ehr-web/README.md)
- [Data README](demos-coghealth-ehr-data/README.md)

## Logs

When using `start.sh`, logs are written to:

```bash
tail -f /tmp/coghealth-web.log   # Frontend logs
tail -f /tmp/coghealth-api.log   # Backend logs
```

## License

Demo Application — Cognition AI