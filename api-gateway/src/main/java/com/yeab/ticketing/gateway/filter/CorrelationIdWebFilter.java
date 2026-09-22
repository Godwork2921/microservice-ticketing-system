package com.yeab.ticketing.gateway.filter;

import com.yeab.ticketing.common.observability.CorrelationHeaders;
import com.yeab.ticketing.common.observability.RequestIdGenerator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getHeaders().getFirst(CorrelationHeaders.REQUEST_ID) != null) {
            return chain.filter(exchange);
        }
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(CorrelationHeaders.REQUEST_ID, RequestIdGenerator.resolveOrCreate(null))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }
}