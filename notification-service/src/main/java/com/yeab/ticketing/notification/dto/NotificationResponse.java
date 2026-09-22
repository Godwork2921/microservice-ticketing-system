package com.yeab.ticketing.notification.dto;

import com.yeab.ticketing.notification.enums.NotificationChannel;
import com.yeab.ticketing.notification.enums.NotificationStatus;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String idempotencyKey, String customerId,
                                  NotificationChannel channel, String recipient, String subject,
                                  String message, NotificationStatus status, String providerMessageId,
                                  String failureReason, Instant createdAt, Instant sentAt, int attemptCount) { }
