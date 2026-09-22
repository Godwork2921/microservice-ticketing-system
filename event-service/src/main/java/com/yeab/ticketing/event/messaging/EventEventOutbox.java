package com.yeab.ticketing.event.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.EventPublishedEvent;
import com.yeab.ticketing.event.config.CorrelationContext;
import com.yeab.ticketing.event.entity.EventEntity;
import com.yeab.ticketing.event.entity.OutboxEntity;
import com.yeab.ticketing.event.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class EventEventOutbox {

    private static final String AGGREGATE = "event";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public EventEventOutbox(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void recordPublished(EventEntity event) {
        EventPublishedEvent payload = new EventPublishedEvent(
                event.getId(), event.getVenueId(), event.getTitle(),
                event.getSeatCount(), event.getStartAt());
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(EventTypes.EVENT_PUBLISHED,
                CorrelationContext.get() == null ? event.getId().toString() : CorrelationContext.get(),
                "event-service", payload);
        outboxRepository.save(OutboxEntity.create(KafkaTopics.EVENT_EVENTS, AGGREGATE, event.getId(), envelope, objectMapper));
    }
}