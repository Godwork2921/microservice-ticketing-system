package com.yeab.ticketing.payment.client;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read-only view of a reservation owned by reservation-service.
 */
public record ReservationDetails(UUID id, String status, BigDecimal totalAmount,
                                 String currency, String customerId, String customerEmail) { }