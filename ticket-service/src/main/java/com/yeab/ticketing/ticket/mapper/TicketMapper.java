package com.yeab.ticketing.ticket.mapper;

import com.yeab.ticketing.ticket.dto.TicketResponse;
import com.yeab.ticketing.ticket.entity.TicketEntity;

public final class TicketMapper {
    private TicketMapper() { }

    public static TicketResponse toResponse(TicketEntity ticket) {
        return new TicketResponse(ticket.getId(), ticket.getTicketCode(), ticket.getReservationId(),
                ticket.getEventId(), ticket.getCustomerId(), ticket.getCustomerEmail(), ticket.getSeatId(),
                ticket.getStatus(), ticket.getIssuedAt(), ticket.getUsedAt(), ticket.getCancelledAt(),
                ticket.getStorageObjectKey());
    }
}