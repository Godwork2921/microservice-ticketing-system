# MinIO

Object storage for ticket artifacts (PDF/QR assets). The `minio-init` service in `docker-compose.yml` creates the `${MINIO_BUCKET}` bucket on first startup.

Environment variables are documented in `.env.example` (`MINIO_ENDPOINT`, credentials, bucket name).
