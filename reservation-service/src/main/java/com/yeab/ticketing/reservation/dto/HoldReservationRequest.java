package com.yeab.ticketing.reservation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Hold request. Prices are NEVER accepted from the client; they are derived
 * server-side from the event's pricing rules and the seat's section.
 *
 * @param idempotencyKey optional, supplied via the {@code Idempotency-Key}
 *                       HTTP header and mapped onto the persisted hold_key.
 */
public record HoldReservationRequest(
        @NotNull UUID eventId,
        @NotEmpty @Size(min = 1, max = 20) List<@NotNull UUID> seatIds,
        String discountCode,
        @NotNull @Size(min = 3, max = 3) String currency,
        String idempotencyKey
) { }