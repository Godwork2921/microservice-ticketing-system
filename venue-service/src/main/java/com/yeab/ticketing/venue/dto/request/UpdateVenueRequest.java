package com.yeab.ticketing.venue.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateVenueRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 500) String address,
        @NotBlank @Size(max = 100) String timezone,
        @Min(1) int capacity
) {
}
