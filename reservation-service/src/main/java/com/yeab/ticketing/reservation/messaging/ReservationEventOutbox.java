package com.yeab.ticketing.reservation.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.ReservationCreatedEvent;
import com.yeab.ticketing.common.events.payload.ReservationStateChangedEvent;
import com.yeab.ticketing.reservation.config.CorrelationContext;
import com.yeab.ticketing.reservation.entity.OutboxEntity;
import com.yeab.ticketing.reservation.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Writes reservation domain events to the outbox table in the same local
 * transaction that mutates the reservation, so the event is never lost even if
 * a later publish attempt fails.
 */
@Component
public class ReservationEventOutbox {

    private static final String AGGREGATE = "reservation";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ReservationEventOutbox(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Records the event after commit by registering a synchronization callback,
     * guaranteeing it is only persisted when the business change itself succeed.
     */
    public void recordReservationCreated(UUID reservationId, UUID eventId, String customerId,
                                         String customerEmail, String currency, java.math.BigDecimal totalAmount,
                                         int seatCount, Instant holdExpiresAt) {
        ReservationCreatedEvent payload = new ReservationCreatedEvent(
                reservationId, eventId, customerId, customerEmail, currency,
                totalAmount, seatCount, holdExpiresAt);
        record(KafkaTopics.RESERVATION_EVENTS, EventTypes.RESERVATION_CREATED, reservationId, payload);
    }

    /**
     * @param eventType one of RESERVATION_CONFIRMED / CANCELLED / EXPIRED; the
     *                  terminal status travels in the envelope, not the payload.
     */
    public void recordReservationStateChanged(String eventType, UUID reservationId, UUID eventId,
                                              String customerId, String customerEmail, String currency,
                                              java.math.BigDecimal totalAmount, int seatCount) {
        ReservationStateChangedEvent payload = new ReservationStateChangedEvent(
                reservationId, eventId, customerId, customerEmail, currency,
                totalAmount, seatCount);
        record(KafkaTopics.RESERVATION_EVENTS, eventType, reservationId, payload);
    }

    private void record(String topic, String eventType, UUID aggregateId, Object payload) {
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(eventType,
                CorrelationContext.get() == null ? aggregateId.toString() : CorrelationContext.get(),
                "reservation-service", payload);
        outboxRepository.save(OutboxEntity.create(topic, AGGREGATE, aggregateId, envelope, objectMapper));
    }
}