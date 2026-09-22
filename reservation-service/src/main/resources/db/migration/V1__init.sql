CREATE TABLE reservations (
	id UUID PRIMARY KEY,
	event_id UUID NOT NULL,
	customer_id VARCHAR(200) NOT NULL,
	customer_email VARCHAR(320) NOT NULL,
	status VARCHAR(30) NOT NULL,
	total_amount NUMERIC(19, 4) NOT NULL,
	currency VARCHAR(3) NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	confirmed_at TIMESTAMPTZ,
	hold_expires_at TIMESTAMPTZ,
	version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE reservation_seats (
	id UUID PRIMARY KEY,
	reservation_id UUID NOT NULL REFERENCES reservations(id),
	event_id UUID NOT NULL,
	seat_id UUID NOT NULL,
	price NUMERIC(19, 4) NOT NULL,
	released BOOLEAN NOT NULL DEFAULT FALSE,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE idempotency_keys (
	id UUID PRIMARY KEY,
	idempotency_key VARCHAR(200) NOT NULL,
	operation VARCHAR(50) NOT NULL,
	reservation_id UUID NOT NULL,
	response_status INTEGER NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT idempotency_unique_key UNIQUE (idempotency_key, operation)
);
