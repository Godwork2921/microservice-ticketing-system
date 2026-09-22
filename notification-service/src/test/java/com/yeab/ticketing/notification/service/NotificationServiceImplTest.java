package com.yeab.ticketing.notification.service;

import com.yeab.ticketing.notification.dto.SendNotificationRequest;
import com.yeab.ticketing.notification.entity.NotificationEntity;
import com.yeab.ticketing.notification.enums.NotificationChannel;
import com.yeab.ticketing.notification.enums.NotificationStatus;
import com.yeab.ticketing.notification.messaging.NotificationEventOutbox;
import com.yeab.ticketing.notification.provider.NotificationProvider;
import com.yeab.ticketing.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock private NotificationRepository repository;
    @Mock private NotificationProvider provider;
    @Mock private NotificationEventOutbox eventOutbox;
    private NotificationServiceImpl service;

    @BeforeEach void setUp() { service = new NotificationServiceImpl(repository, provider, eventOutbox); }

    @Test void sendIsIdempotentByKey() {
        NotificationEntity existing = new NotificationEntity("key-1", "customer-1", NotificationChannel.EMAIL, "a@b.com", "Hi", "Body");
        when(repository.findByIdempotencyKey("key-1")).thenReturn(Optional.of(existing));
        var response = service.send(new SendNotificationRequest("key-1", "customer-1", NotificationChannel.EMAIL, "a@b.com", "Hi", "Body"));
        assertThat(response.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(response.idempotencyKey()).isEqualTo("key-1");
    }

    @Test void sendMarksNotificationSent() {
        when(repository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        when(provider.send(any())).thenReturn("provider-1");
        when(repository.save(any(NotificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.send(new SendNotificationRequest("key-2", "customer-1", NotificationChannel.SMS, "+251900000000", null, "Body"));
        assertThat(response.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.providerMessageId()).isEqualTo("provider-1");
        assertThat(response.attemptCount()).isEqualTo(1);
        verify(eventOutbox).recordSent(any(NotificationEntity.class));
    }

    @Test void failedSendDoesNotRecordSentEvent() {
        when(repository.findByIdempotencyKey("key-3")).thenReturn(Optional.empty());
        when(provider.send(any())).thenThrow(new RuntimeException("provider down"));
        when(repository.save(any(NotificationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = service.send(new SendNotificationRequest("key-3", "customer-1", NotificationChannel.PUSH, "device-token-1", null, "Body"));
        assertThat(response.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("provider down");
        verify(eventOutbox, never()).recordSent(any(NotificationEntity.class));
    }
}
