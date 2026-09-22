package com.yeab.ticketing.venue.exception;

import java.util.UUID;

public class VenueNotFoundException extends RuntimeException {
    public VenueNotFoundException(UUID venueId) {
        super("Venue %s was not found.".formatted(venueId));
    }
}
