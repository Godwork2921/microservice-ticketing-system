package com.yeab.ticketing.reservation.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * View of a venue seat owned by venue-service, fetched over REST.
 */
public record SeatDetails(UUID id, UUID venueId, String section) {

    public static Map<UUID, SeatDetails> indexBySeatId(List<SeatDetails> seats) {
        return seats.stream().collect(Collectors.toUnmodifiableMap(SeatDetails::id, seat -> seat));
    }
}