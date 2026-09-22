package com.yeab.ticketing.ticket.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Read-only view of a reservation owned by reservation-service.
 */
public record ReservationDetails(UUID id, String status, UUID eventId, String customerId,
                                 String customerEmail, List<UUID> seatIds, BigDecimal totalAmount) { }