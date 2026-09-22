package com.yeab.ticketing.common.events.payload;

import java.util.UUID;

/**
 * Emitted by ticket-service after a successful payment so notification-service
 * can deliver the QR to the customer without another round trip.
 */
public record TicketGeneratedEvent(
        UUID ticketId,
        UUID reservationId,
        UUID eventId,
        UUID seatId,
        String customerId,
        String customerEmail,
        String ticketNumber,
        String storageObjectKey
) {
}