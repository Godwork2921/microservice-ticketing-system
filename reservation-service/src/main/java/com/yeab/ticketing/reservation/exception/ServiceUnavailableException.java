package com.yeab.ticketing.reservation.exception;

/**
 * A downstream service (event-service, venue-service) could not be reached.
 * Mapped to 503 SERVICE_UNAVAILABLE.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}