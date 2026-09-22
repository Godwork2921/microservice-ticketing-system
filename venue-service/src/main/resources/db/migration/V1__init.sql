CREATE TABLE venues (
	id UUID PRIMARY KEY,
	name VARCHAR(200) NOT NULL,
	address VARCHAR(500) NOT NULL,
	timezone VARCHAR(100) NOT NULL,
	capacity INTEGER NOT NULL,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT venues_name_not_blank CHECK (length(trim(name)) > 0),
	CONSTRAINT venues_capacity_positive CHECK (capacity > 0)
);

CREATE TABLE seats (
	id UUID PRIMARY KEY,
	venue_id UUID NOT NULL REFERENCES venues(id) ON DELETE CASCADE,
	section VARCHAR(100) NOT NULL,
	seat_row VARCHAR(50) NOT NULL,
	seat_number INTEGER NOT NULL,
	attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
	created_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT seats_section_not_blank CHECK (length(trim(section)) > 0),
	CONSTRAINT seats_row_not_blank CHECK (length(trim(seat_row)) > 0),
	CONSTRAINT seats_number_positive CHECK (seat_number > 0),
	CONSTRAINT seats_unique_position UNIQUE (venue_id, section, seat_row, seat_number)
);

CREATE INDEX idx_seats_venue_order ON seats (venue_id, section, seat_row, seat_number);
CREATE INDEX idx_venues_name ON venues (name);
