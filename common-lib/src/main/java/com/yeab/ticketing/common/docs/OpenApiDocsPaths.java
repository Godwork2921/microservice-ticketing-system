package com.yeab.ticketing.common.docs;

import java.util.List;

public final class OpenApiDocsPaths {

    private static final List<String> PATHS = List.of(
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**"
    );

    private OpenApiDocsPaths() {
    }

    public static String[] publicApiDocsPaths() {
        return PATHS.toArray(new String[0]);
    }
}
