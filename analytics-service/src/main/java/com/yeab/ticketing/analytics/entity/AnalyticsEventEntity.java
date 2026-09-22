package com.yeab.ticketing.analytics.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics_events")
public class AnalyticsEventEntity {
    @Id private UUID id;
    @Column(name = "event_key", nullable = false, unique = true, length = 200) private String eventKey;
    @Column(name = "event_type", nullable = false, length = 100) private String eventType;
    @Column(name = "aggregate_id", length = 200) private String aggregateId;
    @Column(nullable = false, columnDefinition = "jsonb") @JdbcTypeCode(SqlTypes.JSON) private String payload;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;
    @Column(name = "received_at", nullable = false, updatable = false) private Instant receivedAt;
    protected AnalyticsEventEntity() { }
    public AnalyticsEventEntity(String eventKey, String eventType, String aggregateId, String payload, Instant occurredAt) {
        this.eventKey = eventKey; this.eventType = eventType; this.aggregateId = aggregateId; this.payload = payload; this.occurredAt = occurredAt;
    }
    @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); if (receivedAt == null) receivedAt = Instant.now(); }
    public UUID getId() { return id; } public String getEventKey() { return eventKey; } public String getEventType() { return eventType; }
    public String getAggregateId() { return aggregateId; } public String getPayload() { return payload; } public Instant getOccurredAt() { return occurredAt; }
    public Instant getReceivedAt() { return receivedAt; }
}
