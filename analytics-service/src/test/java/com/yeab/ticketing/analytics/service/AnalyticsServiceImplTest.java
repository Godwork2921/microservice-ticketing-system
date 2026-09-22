package com.yeab.ticketing.analytics.service;

import com.yeab.ticketing.analytics.dto.AnalyticsEventRequest;
import com.yeab.ticketing.analytics.entity.AnalyticsEventEntity;
import com.yeab.ticketing.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {
    @Mock private AnalyticsEventRepository repository;
    private AnalyticsServiceImpl service;
    @BeforeEach void setUp() { service = new AnalyticsServiceImpl(repository); }

    @Test void ingestIsIdempotentByEventKey() {
        AnalyticsEventEntity existing = new AnalyticsEventEntity("event-1", "TICKET_ISSUED", "ticket-1", "{}", Instant.now());
        when(repository.findByEventKey("event-1")).thenReturn(Optional.of(existing));
        var result = service.ingest(new AnalyticsEventRequest("event-1", "TICKET_ISSUED", "ticket-1", "{}", Instant.now()));
        assertThat(result.getEventKey()).isEqualTo("event-1");
    }

    @Test void summaryMapsCounts() {
        when(repository.countByEventType()).thenReturn(List.of(new Object[]{"TICKET_ISSUED", 3L}, new Object[]{"PAYMENT_SUCCEEDED", 2L}));
        var result = service.summary();
        assertThat(result).extracting("eventType").containsExactly("TICKET_ISSUED", "PAYMENT_SUCCEEDED");
        assertThat(result.getFirst().count()).isEqualTo(3);
    }

    @Test void overviewAggregatesReadModels() {
        when(repository.countByEventType()).thenReturn(List.of());
        when(repository.sumAmountByEventType("payment.successful")).thenReturn(java.math.BigDecimal.valueOf(2400));
        when(repository.countByEventTypeIn(java.util.List.of("reservation.confirmed"))).thenReturn(7L);
        when(repository.countByEventTypeIn(java.util.List.of("ticket.generated"))).thenReturn(14L);

        var result = service.overview();
        assertThat(result.totalRevenue()).isEqualByComparingTo("2400");
        assertThat(result.confirmedReservations()).isEqualTo(7);
        assertThat(result.ticketsGenerated()).isEqualTo(14);
    }
}
