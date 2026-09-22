package com.yeab.ticketing.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Consistent JSON error body for REST APIs (see spec section 18).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        ErrorCode code,
        String message,
        String path,
        String traceId
) {

    public static ApiErrorResponse of(
            int status,
            ErrorCode code,
            String message,
            String path,
            String traceId
    ) {
        return new ApiErrorResponse(Instant.now(), status, code, message, path, traceId);
    }
}
