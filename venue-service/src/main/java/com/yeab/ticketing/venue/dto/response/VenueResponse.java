package com.yeab.ticketing.venue.dto.response;

import java.time.Instant;
import java.util.UUID;

public record VenueResponse(UUID id, String name, String address, String timezone, int capacity,
                            Instant createdAt, Instant updatedAt) {
}
