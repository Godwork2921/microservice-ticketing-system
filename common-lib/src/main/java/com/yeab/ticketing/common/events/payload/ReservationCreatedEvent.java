package com.yeab.ticketing.common.events.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by reservation-service when a customer successfully holds seats.
 * Carries enough data for payment initiation, notifications and analytics
 * without sharing any JPA entity between services.
 */
public record ReservationCreatedEvent(
        UUID reservationId,
        UUID eventId,
        String customerId,
        String customerEmail,
        String currency,
        BigDecimal totalAmount,
        int seatCount,
        Instant holdExpiresAt
) {
}