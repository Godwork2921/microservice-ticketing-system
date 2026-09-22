package com.yeab.ticketing.notification.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.NotificationSentEvent;
import com.yeab.ticketing.notification.config.CorrelationContext;
import com.yeab.ticketing.notification.entity.NotificationEntity;
import com.yeab.ticketing.notification.entity.OutboxEntity;
import com.yeab.ticketing.notification.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationEventOutbox {

    private static final String AGGREGATE = "notification";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public NotificationEventOutbox(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void recordSent(NotificationEntity notification) {
        NotificationSentEvent payload = new NotificationSentEvent(
                notification.getIdempotencyKey(), notification.getCustomerId(),
                notification.getChannel().name(), notification.getRecipient(),
                notification.getStatus().name());
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(EventTypes.NOTIFICATION_SENT,
                CorrelationContext.get() == null ? UUID.randomUUID().toString() : CorrelationContext.get(),
                "notification-service", payload);
        outboxRepository.save(OutboxEntity.create(KafkaTopics.NOTIFICATION_EVENTS, AGGREGATE,
                notification.getId(), envelope, objectMapper));
    }
}