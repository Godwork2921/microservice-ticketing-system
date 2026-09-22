package com.yeab.ticketing.payment.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.KafkaTopics;
import com.yeab.ticketing.common.events.payload.PaymentFailedEvent;
import com.yeab.ticketing.common.events.payload.PaymentInitiatedEvent;
import com.yeab.ticketing.common.events.payload.PaymentSuccessfulEvent;
import com.yeab.ticketing.payment.config.CorrelationContext;
import com.yeab.ticketing.payment.entity.OutboxEntity;
import com.yeab.ticketing.payment.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class PaymentEventOutbox {

    private static final String AGGREGATE = "payment";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentEventOutbox(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void recordInitiated(UUID paymentId, UUID reservationId, String customerId,
                                String currency, BigDecimal amount, String provider) {
        record(KafkaTopics.PAYMENT_EVENTS, EventTypes.PAYMENT_INITIATED, paymentId,
                new PaymentInitiatedEvent(paymentId, reservationId, customerId, currency, amount, provider));
    }

    public void recordSuccessful(UUID paymentId, UUID reservationId, String customerId,
                                 String currency, BigDecimal amount, String provider, String providerReference) {
        record(KafkaTopics.PAYMENT_EVENTS, EventTypes.PAYMENT_SUCCESSFUL, paymentId,
                new PaymentSuccessfulEvent(paymentId, reservationId, customerId, currency, amount, provider, providerReference));
    }

    public void recordFailed(UUID paymentId, UUID reservationId, String customerId,
                             String currency, BigDecimal amount, String provider, String reason) {
        record(KafkaTopics.PAYMENT_EVENTS, EventTypes.PAYMENT_FAILED, paymentId,
                new PaymentFailedEvent(paymentId, reservationId, customerId, currency, amount, provider, reason));
    }

    private void record(String topic, String eventType, UUID aggregateId, Object payload) {
        DomainEventEnvelope<?> envelope = DomainEventEnvelope.create(eventType,
                CorrelationContext.get() == null ? aggregateId.toString() : CorrelationContext.get(),
                "payment-service", payload);
        outboxRepository.save(OutboxEntity.create(topic, AGGREGATE, aggregateId, envelope, objectMapper));
    }
}