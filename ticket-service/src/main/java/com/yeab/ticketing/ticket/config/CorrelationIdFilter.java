package com.yeab.ticketing.ticket.config;

import com.yeab.ticketing.common.observability.CorrelationHeaders;
import com.yeab.ticketing.common.observability.RequestIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = RequestIdGenerator.resolveOrCreate(request.getHeader(CorrelationHeaders.REQUEST_ID));
        response.setHeader(CorrelationHeaders.REQUEST_ID, requestId);
        MDC.put("traceId", requestId);
        CorrelationContext.set(requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationContext.clear();
            MDC.remove("traceId");
        }
    }
}