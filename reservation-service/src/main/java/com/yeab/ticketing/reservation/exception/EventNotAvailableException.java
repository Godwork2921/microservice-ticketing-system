package com.yeab.ticketing.reservation.exception;

/**
 * Event exists but could not be retrieved from event-service (remote 404).
 * Mapped to 404 RESOURCE_NOT_FOUND.
 */
public class EventNotAvailableException extends RuntimeException {
    public EventNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}