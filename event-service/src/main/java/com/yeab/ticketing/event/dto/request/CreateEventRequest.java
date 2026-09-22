package com.yeab.ticketing.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(
        @NotNull UUID venueId,
        @NotBlank @Size(max = 200) String title,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @Size(max = 10_000) String pricingRules
) {
}
