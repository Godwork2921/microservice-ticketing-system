package com.yeab.ticketing.venue.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeatRequest(
        @NotBlank @Size(max = 100) String section,
        @NotBlank @Size(max = 50) String row,
        @Min(1) int number,
        @Size(max = 10_000) String attributes
) {
}
