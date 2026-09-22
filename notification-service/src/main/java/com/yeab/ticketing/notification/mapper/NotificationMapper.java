package com.yeab.ticketing.notification.mapper;

import com.yeab.ticketing.notification.dto.NotificationResponse;
import com.yeab.ticketing.notification.entity.NotificationEntity;

public final class NotificationMapper {
    private NotificationMapper() { }
    public static NotificationResponse toResponse(NotificationEntity n) {
        return new NotificationResponse(n.getId(), n.getIdempotencyKey(), n.getCustomerId(), n.getChannel(),
                n.getRecipient(), n.getSubject(), n.getMessage(), n.getStatus(), n.getProviderMessageId(),
                n.getFailureReason(), n.getCreatedAt(), n.getSentAt(), n.getAttemptCount());
    }
}
