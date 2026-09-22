package com.yeab.ticketing.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEventEntity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "aggregate_id", length = 200)
    private String aggregateId;
    @Column(length = 200)
    private String topic;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEventEntity() {
    }

    public ProcessedEventEntity(UUID eventId, String eventType, String aggregateId,
                                String topic, Instant occurredAt, Instant processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
    }

    @PrePersist
    void onCreate() {
        if (processedAt == null) processedAt = Instant.now();
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; }
    public String getTopic() { return topic; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getProcessedAt() { return processedAt; }
}