# Stage 1 — Project skeleton and Maven configuration

## Repository layout

The Maven reactor artifact is `backend` at the repository root (equivalent to the spec’s `backend/` folder):

```
.
├── pom.xml                          # Parent BOM + module list
├── common-lib/                      # Shared DTOs/utilities (no domain entities)
├── api-gateway/
├── event-service/
├── venue-service/
├── reservation-service/
├── payment-service/
├── ticket-service/
├── notification-service/
├── analytics-service/
├── user-service/
├── infrastructure/
│   ├── postgres/init/
│   ├── keycloak/
│   ├── kafka/
│   ├── minio/
│   └── docker/
├── docker-compose.yml               # Stage 2 infrastructure
├── .env.example
└── docs/
```

Each service module contains:

- `pom.xml` — independent dependencies, inherits versions from parent
- `src/main/java/.../*Application.java` — Spring Boot entry point
- `src/main/resources/application.yml` — port, datasource, Kafka, security placeholders
- `src/test/java` — unit/integration tests (expanded in Stage 15)

## Design choices

| Decision | Why |
| --- | --- |
| Maven reactor + separate modules | Independently buildable services (`-pl reservation-service`) while sharing dependency versions |
| `common-lib` jar | Event envelope, error contract, correlation headers — without sharing JPA or business logic |
| Spring Boot 4 parent + Spring Cloud BOM | Gateway and future resilience config align on one bill of materials |
| Database-per-service env vars | Same PostgreSQL instance locally, strict ownership in each Flyway migration set |

## Verify Stage 1

```powershell
.\mvnw.cmd -pl common-lib clean test
.\mvnw.cmd clean compile -DskipTests
```

Build one microservice:

```powershell
.\mvnw.cmd -pl event-service -am clean package -DskipTests
```
