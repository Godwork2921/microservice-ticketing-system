package com.yeab.ticketing.ticket.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record IssueTicketsRequest(@NotNull UUID reservationId, @NotNull UUID eventId,
                                  @NotBlank String customerId, @NotEmpty List<@NotNull UUID> seatIds) { }
