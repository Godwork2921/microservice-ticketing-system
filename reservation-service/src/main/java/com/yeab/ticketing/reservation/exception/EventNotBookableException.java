package com.yeab.ticketing.reservation.exception;

/**
 * The event exists but cannot host new reservations (e.g. not PUBLISHED or in
 * the past). Mapped to 422 INVALID_RESERVATION_STATE.
 */
public class EventNotBookableException extends RuntimeException {
    public EventNotBookableException(String message) {
        super(message);
    }
}