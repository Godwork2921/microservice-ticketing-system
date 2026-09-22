package com.yeab.ticketing.event.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.event.entity.OutboxEntity;
import com.yeab.ticketing.event.enums.OutboxStatus;
import com.yeab.ticketing.event.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;
    private final Duration publishTimeout;

    public OutboxPublisher(OutboxRepository outboxRepository,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper,
                           @Value("${app.outbox.batch-size:50}") int batchSize,
                           @Value("${app.outbox.publish-timeout-ms:5000}") long publishTimeoutMs) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
        this.publishTimeout = Duration.ofMillis(publishTimeoutMs);
    }

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:2000}")
    @Transactional
    public void publishPending() {
        List<OutboxEntity> pending = outboxRepository.findPending(PageRequest.of(0, batchSize), OutboxStatus.PENDING);
        for (OutboxEntity outboxEvent : pending) {
            try {
                DomainEventEnvelope<?> envelope = objectMapper.readValue(
                        outboxEvent.getPayload(), DomainEventEnvelope.class);
                kafkaTemplate.send(outboxEvent.getTopic(), envelope.eventId().toString(), envelope)
                        .get(publishTimeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
                outboxEvent.markPublished(Instant.now());
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {} ({})", outboxEvent.getEventType(), outboxEvent.getId(), ex);
                outboxEvent.recordFailure();
            }
        }
    }
}