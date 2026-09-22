package com.yeab.ticketing.analytics.service;

import com.yeab.ticketing.analytics.dto.AnalyticsEventRequest;
import com.yeab.ticketing.analytics.dto.AnalyticsOverview;
import com.yeab.ticketing.analytics.dto.AnalyticsSummary;
import com.yeab.ticketing.analytics.entity.AnalyticsEventEntity;
import com.yeab.ticketing.analytics.repository.AnalyticsEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private final AnalyticsEventRepository repository;
    public AnalyticsServiceImpl(AnalyticsEventRepository repository) { this.repository = repository; }

    @Override @Transactional public AnalyticsEventEntity ingest(AnalyticsEventRequest request) {
        return repository.findByEventKey(request.eventKey()).orElseGet(() -> repository.save(new AnalyticsEventEntity(
                request.eventKey().trim(), request.eventType().trim(), request.aggregateId(), request.payload(), request.occurredAt())));
    }

    @Override @Transactional(readOnly = true) public List<AnalyticsSummary> summary() {
        return repository.countByEventType().stream().map(row -> new AnalyticsSummary((String) row[0], ((Number) row[1]).longValue())).toList();
    }

    @Override @Transactional(readOnly = true) public AnalyticsOverview overview() {
        var byType = repository.countByEventType().stream()
                .map(row -> new AnalyticsSummary((String) row[0], ((Number) row[1]).longValue())).toList();
        return new AnalyticsOverview(
                byType,
                repository.sumAmountByEventType("payment.successful"),
                repository.countByEventTypeIn(List.of("reservation.confirmed")),
                repository.countByEventTypeIn(List.of("ticket.generated")));
    }
}