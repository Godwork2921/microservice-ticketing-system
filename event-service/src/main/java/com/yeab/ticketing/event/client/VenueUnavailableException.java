package com.yeab.ticketing.event.client;

public class VenueUnavailableException extends RuntimeException {
    public VenueUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}