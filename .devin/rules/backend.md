---
trigger: glob
globs: demos-coghealth-ehr-api/**
description: Backend conventions — auto-activates when touching the Spring Boot API
---

- Java 11, Spring Boot 2.7.18, package `com.medchart.ehr`. Layers: `controller` → `service` → `repository` → `domain`, with `dto` + MapStruct `mapper` at the boundary. Lombok for boilerplate.
- The `legacy` package is kept for demo purposes — never copy its patterns into new code.
- Run with the `dev` profile (shared Neon PostgreSQL): `mvn spring-boot:run -Dspring-boot.run.profiles=dev`.
- Hibernate uses `ddl-auto: validate` — every entity change needs a matching, newly-numbered Flyway migration in `src/main/resources/db/migration/`; never edit a migration that already exists.
- After changing endpoints or DTOs, run `/api-contract-drift`. Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`.
