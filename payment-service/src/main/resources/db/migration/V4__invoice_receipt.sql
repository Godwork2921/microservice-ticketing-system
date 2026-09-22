-- Invoice: created when a payment is initiated (represents what is owed)
CREATE TABLE invoices (
    id              UUID PRIMARY KEY,
    payment_id      UUID NOT NULL REFERENCES payments(id),
    reservation_id  UUID NOT NULL,
    customer_id     VARCHAR(200) NOT NULL,
    invoice_number  VARCHAR(50) NOT NULL,
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'ISSUED',
    issued_at       TIMESTAMPTZ NOT NULL,
    due_at          TIMESTAMPTZ,
    notes           VARCHAR(1000),
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT invoices_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT invoices_status_valid CHECK (status IN ('ISSUED', 'PAID', 'VOID')),
    CONSTRAINT invoices_invoice_number_unique UNIQUE (invoice_number),
    CONSTRAINT invoices_payment_unique UNIQUE (payment_id)
);

CREATE INDEX idx_invoices_customer ON invoices (customer_id, issued_at DESC);
CREATE INDEX idx_invoices_reservation ON invoices (reservation_id);

-- Receipt: created when a payment succeeds (proof of payment)
CREATE TABLE receipts (
    id                  UUID PRIMARY KEY,
    payment_id          UUID NOT NULL REFERENCES payments(id),
    invoice_id          UUID REFERENCES invoices(id),
    reservation_id      UUID NOT NULL,
    customer_id         VARCHAR(200) NOT NULL,
    receipt_number      VARCHAR(50) NOT NULL,
    amount_paid         NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    provider            VARCHAR(50) NOT NULL,
    provider_payment_id VARCHAR(200),
    paid_at             TIMESTAMPTZ NOT NULL,
    notes               VARCHAR(1000),
    created_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT receipts_amount_non_negative CHECK (amount_paid >= 0),
    CONSTRAINT receipts_receipt_number_unique UNIQUE (receipt_number),
    CONSTRAINT receipts_payment_unique UNIQUE (payment_id)
);

CREATE INDEX idx_receipts_customer ON receipts (customer_id, paid_at DESC);
CREATE INDEX idx_receipts_reservation ON receipts (reservation_id);
