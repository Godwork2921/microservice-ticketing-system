package com.yeab.ticketing.event.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(UUID eventId) {
        super("Event %s was not found.".formatted(eventId));
    }
}
