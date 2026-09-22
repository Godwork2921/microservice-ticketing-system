ALTER TABLE analytics_events ADD COLUMN topic VARCHAR(200);

CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(200),
    topic VARCHAR(200),
    occurred_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_processed_events_occurred ON processed_events (occurred_at);