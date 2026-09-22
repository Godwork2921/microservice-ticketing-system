package com.yeab.ticketing.common.events.payload;

/**
 * Request for a notification, published by any service on the notification
 * topic. Channel is the canonical string value of the channel (EMAIL, SMS,
 * PUSH) so consuming services do not depend on another service's enum.
 */
public record NotificationRequestedEvent(
        String idempotencyKey,
        String customerId,
        String channel,
        String recipient,
        String subject,
        String message
) { }