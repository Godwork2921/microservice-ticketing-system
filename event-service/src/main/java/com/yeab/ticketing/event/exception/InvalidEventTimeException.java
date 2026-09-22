package com.yeab.ticketing.event.exception;

public class InvalidEventTimeException extends RuntimeException {

    public InvalidEventTimeException() {
        super("Event endAt must be after startAt.");
    }
}
