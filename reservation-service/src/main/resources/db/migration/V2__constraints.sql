ALTER TABLE reservations
	ADD CONSTRAINT reservations_status_valid
	CHECK (status IN ('HOLD', 'CONFIRMED', 'CANCELLED', 'EXPIRED'));

ALTER TABLE reservations
	ADD CONSTRAINT reservations_currency_valid
	CHECK (currency = upper(currency) AND length(currency) = 3);

ALTER TABLE reservation_seats
	ADD CONSTRAINT reservation_seats_price_non_negative CHECK (price >= 0);

CREATE UNIQUE INDEX uq_active_event_seat
	ON reservation_seats (event_id, seat_id)
	WHERE released = FALSE;
