# Event Ticketing Backend

This repository is a Maven reactor (`artifactId`: **backend**) containing independently deployable Spring Boot microservices for the Event Ticketing System.

Shared, non-domain code lives in **`common-lib`** (Kafka event envelope, API error contract, correlation IDs, TTL/LRU cache utilities, retry policy). Services must not share JPA entities or repositories.

Implementation follows staged delivery; see [docs/STAGE-01-SKELETON.md](docs/STAGE-01-SKELETON.md) for the skeleton and Maven layout.

The authoritative file list is [PROJECT_FILE_MANIFEST.md](PROJECT_FILE_MANIFEST.md), regenerated from the live tree with [scripts/generate-manifest.ps1](scripts/generate-manifest.ps1).

## Services

| Module | Port | Responsibility |
| --- | ---: | --- |
| `common-lib` | — | Shared event/error/observability utilities (jar, not a runtime service) |
| `api-gateway` | 8080 | API edge and cross-cutting gateway concerns |
| `event-service` | 8081 | Event catalog ownership |
| `venue-service` | 8082 | Venue and seat ownership |
| `reservation-service` | 8083 | Seat holds, confirmations, cancellations, and expiry |
| `payment-service` | 8084 | Payment lifecycle and provider callbacks |
| `ticket-service` | 8085 | Ticket issuance and validation |
| `notification-service` | 8086 | Notification records and delivery |
| `analytics-service` | 8087 | Event-driven reporting read models |
| `user-service` | 8088 | Application profile data |

## Requirements

- Java 21
- Docker for infrastructure stages

## Build

```powershell
.\\mvnw.cmd clean verify
```

Build one service independently:

```powershell
.\\mvnw.cmd -pl event-service -am clean verify
```

Run one service from its module directory:

```powershell
Set-Location event-service
..\\mvnw.cmd spring-boot:run
```

Each service owns its Java package, configuration, database migrations, and future tests. Services must communicate through APIs or events; they must not share repositories or database tables.

## Local infrastructure

Stage 2 provides PostgreSQL, Kafka in KRaft mode, Keycloak, and MinIO through Docker Compose:

```powershell
Copy-Item .env.example .env
docker compose up -d
docker compose ps
```

Default infrastructure endpoints are PostgreSQL `localhost:5432`, Kafka `localhost:9092`, Keycloak `http://localhost:8180`, MinIO API `http://localhost:9000`, and MinIO Console `http://localhost:9001`.

Stop the stack with `docker compose down`. Use `docker compose down -v` only when you want to remove all local database, Kafka, and object-storage data.

Infrastructure and domain workflows continue in the following implementation stages.
