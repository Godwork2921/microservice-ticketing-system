package com.yeab.ticketing.event.config;

/**
 * Per-thread correlation/request id. Populated by {@link RequestIdFilter} on
 * inbound requests and read when outbox events are recorded so the id survives
 * into the asynchronous Kafka pipeline.
 */
public final class CorrelationContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CorrelationContext() {
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void set(String requestId) {
        CURRENT.set(requestId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}