package com.yeab.ticketing.common.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Canonical Kafka domain-event wrapper shared across services.
 * Producers and consumers must not share JPA entities; this DTO is the contract.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainEventEnvelope<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        String correlationId,
        int version,
        String producer,
        T payload
) {

    public static final int CURRENT_VERSION = 1;

    public DomainEventEnvelope {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(correlationId, "correlationId");
        Objects.requireNonNull(producer, "producer");
        Objects.requireNonNull(payload, "payload");
        if (version < 1) {
            throw new IllegalArgumentException("version must be >= 1");
        }
    }

    public static <T> DomainEventEnvelope<T> create(
            String eventType,
            String correlationId,
            String producer,
            T payload
    ) {
        return new DomainEventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                correlationId,
                CURRENT_VERSION,
                producer,
                payload
        );
    }
}
