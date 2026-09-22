package com.yeab.ticketing.reservation.exception;

/**
 * State transition is not allowed (e.g. confirming a CANCELLED reservation).
 * Mapped to 409 INVALID_RESERVATION_STATE.
 */
public class InvalidReservationStateException extends RuntimeException {
    public InvalidReservationStateException(String message) {
        super(message);
    }
}