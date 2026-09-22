package com.yeab.ticketing.analytics.repository;

import com.yeab.ticketing.analytics.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    @Modifying
    @Query(value = "INSERT INTO processed_events (event_id, event_type, aggregate_id, topic, occurred_at, processed_at) "
            + "VALUES (:eventId, :eventType, :aggregateId, :topic, :occurredAt, :processedAt) "
            + "ON CONFLICT (event_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("eventId") UUID eventId, @Param("eventType") String eventType,
                       @Param("aggregateId") String aggregateId, @Param("topic") String topic,
                       @Param("occurredAt") Instant occurredAt, @Param("processedAt") Instant processedAt);
}