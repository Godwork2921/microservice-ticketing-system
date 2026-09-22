# Local Infrastructure

Stage 2 provides local infrastructure for the ticketing services:

- PostgreSQL 16 with one logical database per service
- Kafka 3.9 in KRaft mode with no ZooKeeper
- Keycloak with the `ticketing` realm and application roles
- MinIO with a bootstrapped `tickets` bucket

Start the stack from the repository root:

```powershell
Copy-Item .env.example .env
.\mvnw.cmd -q validate
docker compose up -d
```

Check service state:

```powershell
docker compose ps
```

Stop the stack:

```powershell
docker compose down
```

To remove local data as well, use `docker compose down -v`. This deletes the PostgreSQL, Kafka, and MinIO volumes.

The default local endpoints are PostgreSQL `localhost:5432`, Kafka `localhost:9092`, Keycloak `http://localhost:8180`, MinIO API `http://localhost:9000`, and MinIO Console `http://localhost:9001`.
