package com.yeab.ticketing.event.dto.request;

import com.yeab.ticketing.event.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record UpdateEventRequest(
        @NotNull UUID venueId,
        @NotBlank @Size(max = 200) String title,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotNull EventStatus status,
        @Size(max = 10_000) String pricingRules
) {
}
