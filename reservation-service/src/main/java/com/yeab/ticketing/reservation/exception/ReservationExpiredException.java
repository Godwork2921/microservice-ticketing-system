package com.yeab.ticketing.reservation.exception;

/**
 * The hold has expired, so the reservation can no longer be confirmed.
 * Mapped to 409 RESERVATION_EXPIRED.
 */
public class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(String message) {
        super(message);
    }
}