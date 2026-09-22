package com.yeab.ticketing.analytics.messaging;

import com.yeab.ticketing.analytics.config.CorrelationContext;
import com.yeab.ticketing.analytics.dto.AnalyticsEventRequest;
import com.yeab.ticketing.analytics.repository.ProcessedEventRepository;
import com.yeab.ticketing.analytics.service.AnalyticsService;
import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumes every canonical domain topic, dedupes on the envelope event id via
 * the {@code processed_events} table, and persists a copy for analytics.
 */
@Component
public class AnalyticsEventListener {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventListener.class);

    private static final String[] AGGREGATE_ID_KEYS =
            {"reservationId", "paymentId", "ticketId", "eventId", "seatId"};

    private final AnalyticsService service;
    private final ProcessedEventRepository processedEvents;
    private final ObjectMapper objectMapper;

    public AnalyticsEventListener(AnalyticsService service,
                                  ProcessedEventRepository processedEvents,
                                  ObjectMapper objectMapper) {
        this.service = service;
        this.processedEvents = processedEvents;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "#{'${app.kafka.topics:ticketing.reservation.events,ticketing.payment.events,ticketing.ticket.events,ticketing.event.events,ticketing.notification.events}'.split(',')}",
            groupId = "${spring.kafka.consumer.group-id:analytics-service}")
    @Transactional
    public void onDomainEvent(DomainEventEnvelope envelope,
                              @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        CorrelationContext.set(envelope.correlationId());
        try {
            boolean inserted = processedEvents.insertIfAbsent(
                    envelope.eventId(), envelope.eventType(), aggregateIdOf(envelope),
                    topic, envelope.occurredAt(), Instant.now()) > 0;
            if (!inserted) {
                log.debug("Dropping duplicate event {}", envelope.eventId());
                return;
            }
            service.ingest(new AnalyticsEventRequest(
                    envelope.eventId().toString(), envelope.eventType(),
                    aggregateIdOf(envelope),
                    objectMapper.writeValueAsString(envelope.payload()),
                    envelope.occurredAt()));
        } catch (Exception ex) {
            log.error("Failed to process domain event {} from {}", envelope.eventId(), topic, ex);
        } finally {
            CorrelationContext.clear();
        }
    }

    private String aggregateIdOf(DomainEventEnvelope envelope) {
        try {
            JsonNode node = objectMapper.valueToTree(envelope.payload());
            for (String key : AGGREGATE_ID_KEYS) {
                JsonNode value = node.get(key);
                if (value != null && !value.isNull()) {
                    return value.asText();
                }
            }
        } catch (Exception ignored) {
            // payload not a JSON object; fall through
        }
        return envelope.producer();
    }
}