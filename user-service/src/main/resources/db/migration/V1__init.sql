CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(40),
    locale VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT users_email_not_blank CHECK (length(trim(email)) > 0),
    CONSTRAINT users_first_name_not_blank CHECK (length(trim(first_name)) > 0)
);

CREATE UNIQUE INDEX uq_users_email ON users (lower(email));
CREATE INDEX idx_users_name ON users (lower(first_name), lower(last_name));