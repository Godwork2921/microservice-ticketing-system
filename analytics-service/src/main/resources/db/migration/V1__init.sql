CREATE TABLE analytics_events (
	id UUID PRIMARY KEY,
	event_key VARCHAR(200) NOT NULL UNIQUE,
	event_type VARCHAR(100) NOT NULL,
	aggregate_id VARCHAR(200),
	payload JSONB NOT NULL,
	occurred_at TIMESTAMPTZ NOT NULL,
	received_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_analytics_event_type_time ON analytics_events (event_type, occurred_at DESC);
CREATE INDEX idx_analytics_aggregate ON analytics_events (aggregate_id);
