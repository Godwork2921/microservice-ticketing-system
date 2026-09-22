package com.yeab.ticketing.venue.exception;

public class InvalidSeatAttributesException extends RuntimeException {
    public InvalidSeatAttributesException() {
        super("attributes must be a valid JSON object.");
    }
}
