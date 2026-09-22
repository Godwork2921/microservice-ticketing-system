package com.yeab.ticketing.ticket.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.NotificationRequestedEvent;
import com.yeab.ticketing.common.events.payload.TicketGeneratedEvent;
import com.yeab.ticketing.ticket.config.CorrelationContext;
import com.yeab.ticketing.ticket.entity.OutboxEntity;
import com.yeab.ticketing.ticket.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class TicketEventOutbox {

    private static final String AGGREGATE = "ticket";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public TicketEventOutbox(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void recordGenerated(UUID ticketId, UUID reservationId, UUID eventId, UUID seatId,
                                String customerId, String customerEmail, String ticketNumber,
                                String storageObjectKey) {
        TicketGeneratedEvent payload = new TicketGeneratedEvent(ticketId, reservationId, eventId,
                seatId, customerId, customerEmail, ticketNumber, storageObjectKey);
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(EventTypes.TICKET_GENERATED,
                CorrelationContext.get() == null ? ticketId.toString() : CorrelationContext.get(),
                "ticket-service", payload);
        outboxRepository.save(OutboxEntity.create(KafkaTopics.TICKET_EVENTS, AGGREGATE, ticketId, envelope, objectMapper));
    }

    /**
     * Requests an email delivery from notification-service once tickets have been
     * issued for a reservation. The idempotency key is derived from the reservation
     * so a re-delivery of the same payment event never duplicates the notification.
     */
    public void recordRequested(String idempotencyKey, String customerId, String channel,
                                String recipient, String subject, String message) {
        NotificationRequestedEvent payload = new NotificationRequestedEvent(
                idempotencyKey, customerId, channel, recipient, subject, message);
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(EventTypes.NOTIFICATION_REQUESTED,
                CorrelationContext.get() == null ? idempotencyKey : CorrelationContext.get(),
                "ticket-service", payload);
        outboxRepository.save(OutboxEntity.create(KafkaTopics.NOTIFICATION_EVENTS, "notification",
                UUID.nameUUIDFromBytes(idempotencyKey.getBytes(StandardCharsets.UTF_8)), envelope, objectMapper));
    }
}