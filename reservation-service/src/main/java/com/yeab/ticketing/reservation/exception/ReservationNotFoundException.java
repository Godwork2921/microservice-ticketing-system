package com.yeab.ticketing.reservation.exception;

/**
 * Raised when the event no longer exists remotely. Mapped to 404 RESOURCE_NOT_FOUND.
 */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}