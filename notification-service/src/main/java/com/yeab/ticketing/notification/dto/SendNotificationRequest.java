package com.yeab.ticketing.notification.dto;

import com.yeab.ticketing.notification.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(@NotBlank String idempotencyKey, @NotBlank String customerId,
                                      @NotNull NotificationChannel channel, @NotBlank String recipient,
                                      String subject, @NotBlank String message) { }
