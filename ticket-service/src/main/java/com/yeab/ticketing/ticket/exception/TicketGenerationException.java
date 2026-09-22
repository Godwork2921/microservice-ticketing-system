package com.yeab.ticketing.ticket.exception;

/**
 * Ticket generation failed (downstream reservation lookup, QR rendering,
 * object storage). Mapped to 500 TICKET_GENERATION_FAILURE.
 */
public class TicketGenerationException extends RuntimeException {
    public TicketGenerationException(String message) {
        super(message);
    }
}