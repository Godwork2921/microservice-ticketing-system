package com.yeab.ticketing.notification.messaging;

import com.yeab.ticketing.common.events.DomainEventEnvelope;
import com.yeab.ticketing.common.events.EventTypes;
import com.yeab.ticketing.common.events.payload.NotificationRequestedEvent;
import com.yeab.ticketing.notification.dto.SendNotificationRequest;
import com.yeab.ticketing.notification.enums.NotificationChannel;
import com.yeab.ticketing.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationEventListenerTest {
    @Test
    void kafkaEventDelegatesToNotificationService() {
        NotificationService service = mock(NotificationService.class);
        NotificationEventListener listener = new NotificationEventListener(service, new ObjectMapper());
        NotificationRequestedEvent payload = new NotificationRequestedEvent("kafka-key", "customer-1",
                "EMAIL", "customer@example.com", "Ticket", "Your ticket is ready");
        DomainEventEnvelope<NotificationRequestedEvent> envelope = DomainEventEnvelope.create(
                EventTypes.NOTIFICATION_REQUESTED, UUID.randomUUID().toString(), "ticket-service", payload);

        listener.onNotificationEvent(envelope);

        ArgumentCaptor<SendNotificationRequest> captor = ArgumentCaptor.forClass(SendNotificationRequest.class);
        verify(service).send(captor.capture());
        assertThat(captor.getValue().idempotencyKey()).isEqualTo("kafka-key");
        assertThat(captor.getValue().channel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(captor.getValue().recipient()).isEqualTo("customer@example.com");
    }

    @Test
    void ignoresNonRequestedEvents() {
        NotificationService service = mock(NotificationService.class);
        NotificationEventListener listener = new NotificationEventListener(service, new ObjectMapper());
        DomainEventEnvelope<Object> envelope = DomainEventEnvelope.create(
                EventTypes.NOTIFICATION_SENT, UUID.randomUUID().toString(), "notification-service", "{}");

        listener.onNotificationEvent(envelope);

        verify(service, org.mockito.Mockito.never())
                .send(org.mockito.ArgumentMatchers.any(SendNotificationRequest.class));
    }
}