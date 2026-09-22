package com.yeab.ticketing.reservation.exception;

/**
 * A seat was already held/confirmed by another reservation (race over the unique
 * active-seat index). Mapped to 409 SEAT_ALREADY_RESERVED.
 */
public class SeatAlreadyReservedException extends RuntimeException {
    public SeatAlreadyReservedException(String message) {
        super(message);
    }
}