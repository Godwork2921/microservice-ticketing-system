CREATE TABLE payments (
	id UUID PRIMARY KEY,
	reservation_id UUID NOT NULL,
	customer_id VARCHAR(200) NOT NULL,
	amount NUMERIC(19, 4) NOT NULL,
	currency VARCHAR(3) NOT NULL,
	status VARCHAR(30) NOT NULL,
	provider VARCHAR(50) NOT NULL,
	provider_payment_id VARCHAR(200),
	failure_reason VARCHAR(500),
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	completed_at TIMESTAMPTZ,
	version BIGINT NOT NULL DEFAULT 0,
	CONSTRAINT payments_amount_non_negative CHECK (amount >= 0),
	CONSTRAINT payments_currency_valid CHECK (currency = upper(currency) AND length(currency) = 3),
	CONSTRAINT payments_status_valid CHECK (status IN ('PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
	CONSTRAINT payments_reservation_unique UNIQUE (reservation_id)
);

CREATE INDEX idx_payments_customer_created ON payments (customer_id, created_at DESC);
CREATE INDEX idx_payments_status ON payments (status);
