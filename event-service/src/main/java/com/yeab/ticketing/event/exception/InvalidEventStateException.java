package com.yeab.ticketing.event.exception;

import com.yeab.ticketing.event.enums.EventStatus;

public class InvalidEventStateException extends RuntimeException {

    public InvalidEventStateException(EventStatus current, EventStatus requested) {
        super("Event cannot transition from %s to %s.".formatted(current, requested));
    }
}
