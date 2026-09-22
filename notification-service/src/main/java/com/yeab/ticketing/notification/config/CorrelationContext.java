package com.yeab.ticketing.notification.config;

/**
 * Per-thread trace/request id. Populated on inbound requests and when
 * consuming Kafka events, read back when recording outbox entries so the id
 * survives into the async pipeline.
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