package com.yeab.ticketing.venue.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SeatResponse(UUID id, UUID venueId, String section, String row, int number,
                           String attributes, Instant createdAt) {
}
