package com.yeab.ticketing.payment.config;

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