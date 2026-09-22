package com.yeab.ticketing.notification.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.NotificationRequestedEvent;
import com.yeab.ticketing.notification.config.CorrelationContext;
import com.yeab.ticketing.notification.dto.SendNotificationRequest;
import com.yeab.ticketing.notification.enums.NotificationChannel;
import com.yeab.ticketing.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Consumes canonical {@code ticketing.notification.events} envelopes and only
 * reacts to {@code notification.requested}. Cross-service payloads carry the
 * channel as a plain string so payload serialization does not depend on a
 * service-owned enum.
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS,
            groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void onNotificationEvent(DomainEventEnvelope envelope) {
        if (!EventTypes.NOTIFICATION_REQUESTED.equals(envelope.eventType())) {
            return;
        }
        CorrelationContext.set(envelope.correlationId());
        try {
            NotificationRequestedEvent event = objectMapper.convertValue(envelope.payload(), NotificationRequestedEvent.class);
            notificationService.send(new SendNotificationRequest(event.idempotencyKey(), event.customerId(),
                    NotificationChannel.valueOf(event.channel().toUpperCase(Locale.ROOT)),
                    event.recipient(), event.subject(), event.message()));
        } catch (Exception ex) {
            log.error("Failed to process notification.requested event {}", envelope.eventId(), ex);
        } finally {
            CorrelationContext.clear();
        }
    }
}