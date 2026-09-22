package com.yeab.ticketing.common.observability;

import java.util.UUID;

public final class RequestIdGenerator {

    private RequestIdGenerator() {
    }

    public static String newRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String resolveOrCreate(String incoming) {
        if (incoming == null || incoming.isBlank()) {
            return newRequestId();
        }
        return incoming.trim();
    }
}
