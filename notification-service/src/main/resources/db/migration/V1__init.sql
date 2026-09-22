CREATE TABLE notifications (
	id UUID PRIMARY KEY,
	idempotency_key VARCHAR(200) NOT NULL UNIQUE,
	customer_id VARCHAR(200) NOT NULL,
	channel VARCHAR(20) NOT NULL,
	recipient VARCHAR(320) NOT NULL,
	subject VARCHAR(300),
	message TEXT NOT NULL,
	status VARCHAR(30) NOT NULL,
	provider_message_id VARCHAR(200),
	failure_reason VARCHAR(500),
	created_at TIMESTAMPTZ NOT NULL,
	sent_at TIMESTAMPTZ,
	attempt_count INTEGER NOT NULL DEFAULT 0,
	version BIGINT NOT NULL DEFAULT 0,
	CONSTRAINT notifications_channel_valid CHECK (channel IN ('EMAIL', 'SMS')),
	CONSTRAINT notifications_status_valid CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);
CREATE INDEX idx_notifications_customer_created ON notifications (customer_id, created_at DESC);
CREATE INDEX idx_notifications_status ON notifications (status);
