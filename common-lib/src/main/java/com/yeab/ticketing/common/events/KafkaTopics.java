package com.yeab.ticketing.common.events;

/**
 * Kafka topic names for the ticketing platform.
 * Services may override via configuration, but these defaults keep producers/consumers aligned.
 */
public final class KafkaTopics {

    public static final String RESERVATION_EVENTS = "ticketing.reservation.events";
    public static final String PAYMENT_EVENTS = "ticketing.payment.events";
    public static final String TICKET_EVENTS = "ticketing.ticket.events";
    public static final String NOTIFICATION_EVENTS = "ticketing.notification.events";
    public static final String ANALYTICS_EVENTS = "ticketing.analytics.events";
    public static final String EVENT_EVENTS = "ticketing.event.events";

    private KafkaTopics() {
    }
}
