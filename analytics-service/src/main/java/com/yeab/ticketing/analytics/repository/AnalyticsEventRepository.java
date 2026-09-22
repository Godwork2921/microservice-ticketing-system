package com.yeab.ticketing.analytics.repository;

import com.yeab.ticketing.analytics.entity.AnalyticsEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEventEntity, UUID> {
    Optional<AnalyticsEventEntity> findByEventKey(String eventKey);
    @Query("select e.eventType, count(e) from AnalyticsEventEntity e group by e.eventType order by count(e) desc")
    List<Object[]> countByEventType();
    long countByEventTypeIn(Collection<String> eventTypes);
    @Query(value = "SELECT COALESCE(SUM(CAST(payload ->> 'amount' AS numeric)), 0) "
            + "FROM analytics_events WHERE event_type = :eventType", nativeQuery = true)
    BigDecimal sumAmountByEventType(@Param("eventType") String eventType);
}
