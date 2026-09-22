package com.yeab.ticketing.venue.exception;

import java.util.UUID;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(UUID seatId) {
        super("Seat %s was not found.".formatted(seatId));
    }
}