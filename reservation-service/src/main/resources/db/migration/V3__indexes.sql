CREATE INDEX idx_reservations_customer_created ON reservations (customer_id, created_at DESC);
CREATE INDEX idx_reservations_hold_expiry ON reservations (hold_expires_at)
	WHERE status = 'HOLD';
CREATE INDEX idx_reservation_seats_reservation ON reservation_seats (reservation_id);
