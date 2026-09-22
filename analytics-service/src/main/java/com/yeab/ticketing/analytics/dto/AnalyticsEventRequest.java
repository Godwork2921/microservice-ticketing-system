package com.yeab.ticketing.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AnalyticsEventRequest(@NotBlank String eventKey, @NotBlank String eventType,
                                    String aggregateId, @NotBlank String payload, @NotNull Instant occurredAt) { }
