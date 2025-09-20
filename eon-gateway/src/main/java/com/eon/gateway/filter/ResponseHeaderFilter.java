package com.eon.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * 响应头安全强化（基本安全头）。
 */
@Component
public class ResponseHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() { return 500; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse resp = exchange.getResponse();
            HttpHeaders h = resp.getHeaders();
            h.addIfAbsent("X-Content-Type-Options", "nosniff");
            h.addIfAbsent("X-Frame-Options", "DENY");
            h.addIfAbsent("X-XSS-Protection", "1; mode=block");
            // 如有 https，可考虑 HSTS：h.addIfAbsent("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }));
    }
}

