package com.yeab.ticketing.common.events.payload;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Emitted by payment-service when a payment fails.
 */
public record PaymentFailedEvent(
        UUID paymentId,
        UUID reservationId,
        String customerId,
        String currency,
        BigDecimal amount,
        String provider,
        String reason
) {
}