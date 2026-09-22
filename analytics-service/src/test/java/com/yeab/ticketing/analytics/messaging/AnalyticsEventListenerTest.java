package com.yeab.ticketing.analytics.messaging;

import com.yeab.ticketing.analytics.repository.ProcessedEventRepository;
import com.yeab.ticketing.analytics.service.AnalyticsService;
import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.payload.PaymentSuccessfulEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsEventListenerTest {
    @Test void firstOccurrenceDelegatesToAnalyticsService() throws Exception {
        AnalyticsService service = mock(AnalyticsService.class);
        ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
        AnalyticsEventListener listener = new AnalyticsEventListener(service, processedEvents, new ObjectMapper());
        PaymentSuccessfulEvent payload = new PaymentSuccessfulEvent(UUID.randomUUID(), UUID.randomUUID(),
                "customer-1", "ETB", new BigDecimal("1200.00"), "mock", "prov-ref-1");
        DomainEventEnvelope<PaymentSuccessfulEvent> envelope = DomainEventEnvelope.create(
                EventTypes.PAYMENT_SUCCESSFUL, "corr-1", "payment-service", payload);
        when(processedEvents.insertIfAbsent(any(), any(), any(), any(), any(), any())).thenReturn(1);

        listener.onDomainEvent(envelope, "ticketing.payment.events");

        ArgumentCaptor<com.yeab.ticketing.analytics.dto.AnalyticsEventRequest> captor =
                ArgumentCaptor.forClass(com.yeab.ticketing.analytics.dto.AnalyticsEventRequest.class);
        verify(service).ingest(captor.capture());
        assertThat(captor.getValue().eventKey()).isEqualTo(envelope.eventId().toString());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.PAYMENT_SUCCESSFUL);
        assertThat(captor.getValue().aggregateId()).isEqualTo(payload.reservationId().toString());
    }

    @Test void duplicateEventIdIsDropped() {
        AnalyticsService service = mock(AnalyticsService.class);
        ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
        AnalyticsEventListener listener = new AnalyticsEventListener(service, processedEvents, new ObjectMapper());
        when(processedEvents.insertIfAbsent(any(), any(), any(), any(), any(), any())).thenReturn(0);

        listener.onDomainEvent(DomainEventEnvelope.create(EventTypes.PAYMENT_SUCCESSFUL,
                "corr-2", "payment-service", "{}"), "ticketing.payment.events");

        verify(service, never()).ingest(any());
    }
}