package com.yeab.ticketing.common.events.payload;

/**
 * Audit payload emitted on the notification topic after a notification has
 * been delivered, so analytics can count delivers without querying the
 * notification database.
 */
public record NotificationSentEvent(
        String idempotencyKey,
        String customerId,
        String channel,
        String recipient,
        String status
) { }