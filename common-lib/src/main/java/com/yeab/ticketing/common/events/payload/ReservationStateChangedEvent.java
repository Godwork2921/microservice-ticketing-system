package com.yeab.ticketing.common.events.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted when a reservation transitions out of HOLD: CONFIRMED, CANCELLED or EXPIRED.
 * The terminal status is exposed by the envelope's eventType; this payload is
 * the aggregate state change summary.
 */
public record ReservationStateChangedEvent(
        UUID reservationId,
        UUID eventId,
        String customerId,
        String customerEmail,
        String currency,
        BigDecimal totalAmount,
        int seatCount
) {
}