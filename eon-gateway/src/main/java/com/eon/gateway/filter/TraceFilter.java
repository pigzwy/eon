package com.eon.gateway.filter;

import com.eon.gateway.security.AuthConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = AuthConstants.HDR_X_TRACE_ID;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        String finalTraceId = traceId;
        exchange.getAttributes().put(AuthConstants.ATTR_TRACE_ID, finalTraceId);
        exchange.getResponse().getHeaders().set(TRACE_ID_HEADER, finalTraceId);
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.headers(headers -> headers.set(TRACE_ID_HEADER, finalTraceId)))
                .build();
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() {
        return -20;
    }
}
