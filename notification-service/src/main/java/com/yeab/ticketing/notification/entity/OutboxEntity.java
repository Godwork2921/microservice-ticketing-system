package com.yeab.ticketing.notification.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.notification.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 200)
    private String topic;
    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "correlation_id", length = 200)
    private String correlationId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutboxStatus status;
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Version
    private long version;

    protected OutboxEntity() {
    }

    public static OutboxEntity create(String topic, String aggregateType, UUID aggregateId,
                                      DomainEventEnvelope<?> envelope, ObjectMapper mapper) {
        try {
            OutboxEntity entity = new OutboxEntity();
            entity.topic = topic;
            entity.aggregateType = aggregateType;
            entity.aggregateId = aggregateId;
            entity.eventType = envelope.eventType();
            entity.correlationId = envelope.correlationId();
            entity.payload = mapper.writeValueAsString(envelope);
            entity.status = OutboxStatus.PENDING;
            entity.attemptCount = 0;
            return entity;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getPayload() { return payload; }
    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getEventType() { return eventType; }
    public String getCorrelationId() { return correlationId; }
    public OutboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public Instant getPublishedAt() { return publishedAt; }
    public void markPublished(Instant publishedAt) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
    }
    public void recordFailure() {
        this.attemptCount++;
        this.status = OutboxStatus.FAILED;
    }
    public void markPendingForRetry() {
        this.attemptCount++;
        this.status = OutboxStatus.PENDING;
    }
}