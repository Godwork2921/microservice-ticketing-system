package com.yeab.ticketing.common.events.payload;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by payment-service when a payment is created for a reservation.
 */
public record PaymentInitiatedEvent(
        UUID paymentId,
        UUID reservationId,
        String customerId,
        String currency,
        BigDecimal amount,
        String provider
) {
}