# Docker

Stage 17 adds multi-stage Dockerfiles per service and extends `docker-compose.yml` with application containers.

Stage 2 uses Compose for infrastructure only (PostgreSQL, Kafka, Keycloak, MinIO). Application services run via Maven during development unless you enable the full stack profile in a later stage.
