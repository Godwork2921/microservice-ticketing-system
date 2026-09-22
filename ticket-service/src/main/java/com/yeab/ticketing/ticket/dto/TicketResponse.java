package com.yeab.ticketing.ticket.dto;

import com.yeab.ticketing.ticket.enums.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketResponse(UUID id, String ticketCode, UUID reservationId, UUID eventId,
                             String customerId, String customerEmail, UUID seatId, TicketStatus status,
                             Instant issuedAt, Instant usedAt, Instant cancelledAt,
                             String storageObjectKey) { }