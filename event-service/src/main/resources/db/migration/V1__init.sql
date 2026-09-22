CREATE TABLE events (
	id UUID PRIMARY KEY,
	venue_id UUID NOT NULL,
	title VARCHAR(200) NOT NULL,
	start_at TIMESTAMPTZ NOT NULL,
	end_at TIMESTAMPTZ NOT NULL,
	status VARCHAR(30) NOT NULL,
	pricing_rules JSONB NOT NULL DEFAULT '{}'::jsonb,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT events_title_not_blank CHECK (length(trim(title)) > 0),
	CONSTRAINT events_time_order CHECK (end_at > start_at),
	CONSTRAINT events_status_valid CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED'))
);

CREATE INDEX idx_events_start_at ON events (start_at);
CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_events_status_start_at ON events (status, start_at);
