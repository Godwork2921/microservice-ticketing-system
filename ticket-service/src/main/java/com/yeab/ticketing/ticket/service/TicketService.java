package com.yeab.ticketing.ticket.service;

import com.yeab.ticketing.ticket.client.ReservationDetails;
import com.yeab.ticketing.ticket.dto.IssueTicketsRequest;
import com.yeab.ticketing.ticket.dto.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    /**
     * Dev/test path: issue tickets for an already-paid reservation without the
     * event bus. Production flow uses {@link #generateForReservation}.
     */
    List<TicketResponse> issue(IssueTicketsRequest request);

    /**
     * Event-driven path invoked by the payment listener once the reservation is
     * CONFIRMED. Idempotent per reservation.
     */
    List<TicketResponse> generateForReservation(ReservationDetails reservation);

    TicketResponse getById(UUID id);

    List<TicketResponse> byReservation(UUID id);

    Page<TicketResponse> list(String customerId, Pageable pageable);

    TicketResponse use(UUID id);

    TicketResponse cancel(UUID id);

    /**
     * Validates a ticket by its code; only ISSUED tickets validate cleanly.
     */
    TicketResponse validate(String ticketCode);

    /**
     * Returns the stored QR PNG for a ticket.
     */
    byte[] qrImage(UUID id);
}