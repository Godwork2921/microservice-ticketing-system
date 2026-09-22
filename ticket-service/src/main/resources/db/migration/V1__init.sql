CREATE TABLE tickets (
	id UUID PRIMARY KEY,
	ticket_code VARCHAR(100) NOT NULL UNIQUE,
	reservation_id UUID NOT NULL,
	event_id UUID NOT NULL,
	customer_id VARCHAR(200) NOT NULL,
	seat_id UUID NOT NULL,
	status VARCHAR(30) NOT NULL,
	issued_at TIMESTAMPTZ NOT NULL,
	used_at TIMESTAMPTZ,
	cancelled_at TIMESTAMPTZ,
	version BIGINT NOT NULL DEFAULT 0,
	CONSTRAINT tickets_status_valid CHECK (status IN ('ISSUED', 'USED', 'CANCELLED')),
	CONSTRAINT tickets_reservation_seat_unique UNIQUE (reservation_id, seat_id)
);
CREATE INDEX idx_tickets_customer ON tickets (customer_id, issued_at DESC);
CREATE INDEX idx_tickets_event ON tickets (event_id);
