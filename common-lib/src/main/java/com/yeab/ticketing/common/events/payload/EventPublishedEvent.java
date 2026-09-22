package com.yeab.ticketing.common.events.payload;

import java.time.Instant;
import java.util.UUID;

/**
 * Emitted by event-service when an event becomes {@code PUBLISHED}.
 * Carries the seat capacity so analytics can compute occupancy without
 * synchronously querying venue-service.
 */
public record EventPublishedEvent(
        UUID eventId,
        UUID venueId,
        String title,
        int seatCount,
        Instant startAt
) {
}