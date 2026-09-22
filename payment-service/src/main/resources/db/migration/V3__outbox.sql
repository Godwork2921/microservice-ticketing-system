CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    topic VARCHAR(200) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(200),
    payload JSONB NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT outbox_status_valid CHECK (status IN ('PENDING', 'PUBLISHED', 'FAILED'))
);

CREATE INDEX idx_outbox_pending ON outbox_events (status, created_at) WHERE status = 'PENDING';