package com.yeab.ticketing.common.observability;

/**
 * HTTP header names propagated by the API gateway and downstream services.
 */
public final class CorrelationHeaders {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CORRELATION_ID = "X-Correlation-Id";

    private CorrelationHeaders() {
    }
}
