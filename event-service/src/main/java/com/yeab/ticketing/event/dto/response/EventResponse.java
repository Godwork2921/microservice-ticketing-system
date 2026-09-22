package com.yeab.ticketing.event.dto.response;

import com.yeab.ticketing.event.enums.EventStatus;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID venueId,
        String title,
        Instant startAt,
        Instant endAt,
        EventStatus status,
        String pricingRules,
        int seatCount,
        Instant createdAt,
        Instant updatedAt
) {
}
