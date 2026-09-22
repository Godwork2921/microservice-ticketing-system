package com.yeab.ticketing.common.events;

/**
 * Stable domain event type strings used as {@code eventType} on {@link DomainEventEnvelope}.
 */
public final class EventTypes {

    public static final String RESERVATION_CREATED = "reservation.created";
    public static final String RESERVATION_CONFIRMED = "reservation.confirmed";
    public static final String RESERVATION_CANCELLED = "reservation.cancelled";
    public static final String RESERVATION_EXPIRED = "reservation.expired";

    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_SUCCESSFUL = "payment.successful";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String TICKET_GENERATED = "ticket.generated";
    public static final String NOTIFICATION_REQUESTED = "notification.requested";
    public static final String NOTIFICATION_SENT = "notification.sent";

    public static final String EVENT_PUBLISHED = "event.published";

    private EventTypes() {
    }
}
