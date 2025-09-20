package com.eon.gateway.filter;

import com.eon.gateway.config.GatewaySecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证网关在解码JWT后能够提取自定义声明并透传到下游。
 */
class JwtAuthenticationFilterTest {

    @Test
    void filter_should_extract_user_roles_and_permissions() {
        GatewaySecurityProperties props = new GatewaySecurityProperties();
        Jwt jwt = Jwt.withTokenValue("token-value")
                .header("alg", "RS256")
                .claim("uid", "1001")
                .claim("tenant", "tenant-a")
                .claim("pv", "p1")
                .claim("roles", List.of("ADMIN", "USER"))
                .claim("permissions", List.of("user:read", "user:write"))
                .build();
        ReactiveJwtDecoder decoder = token -> Mono.just(jwt);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(props, decoder);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/demo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-value")
                .header("X-User-Id", "spoof-id")
                .header("X-User-Roles", "spoof-role")
                .header("X-Roles", "spoof-role")
                .header("X-User-Permissions", "spoof")
                .header("X-Permissions", "spoof")
                .header("X-Tenant-Id", "spoof-tenant")
                .header("X-Policy-Version", "spoof-policy")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        CapturingChain chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) exchange.getAttributes().get(JwtAuthenticationFilter.ATTR_ROLES);
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) exchange.getAttributes().get(JwtAuthenticationFilter.ATTR_PERMISSIONS);

        assertEquals(List.of("ADMIN", "USER"), roles);
        assertEquals(List.of("user:read", "user:write"), permissions);

        ServerHttpRequest mutated = chain.getExchange().getRequest();
        assertEquals("1001", mutated.getHeaders().getFirst("X-User-Id"));
        assertEquals("ADMIN,USER", mutated.getHeaders().getFirst("X-User-Roles"));
        assertEquals("ADMIN,USER", mutated.getHeaders().getFirst("X-Roles"));
        assertEquals("user:read,user:write", mutated.getHeaders().getFirst("X-User-Permissions"));
        assertEquals("user:read,user:write", mutated.getHeaders().getFirst("X-Permissions"));
        assertEquals(1, mutated.getHeaders().get("X-User-Id").size());
        assertEquals("tenant-a", mutated.getHeaders().getFirst("X-Tenant-Id"));
        assertEquals("p1", mutated.getHeaders().getFirst("X-Policy-Version"));
    }

    /**
     * 简单的过滤器链桩对象，用于捕获被传递的请求。
     */
    private static final class CapturingChain implements GatewayFilterChain {

        private ServerWebExchange exchange;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.exchange = exchange;
            return Mono.empty();
        }

        ServerWebExchange getExchange() {
            assertNotNull(exchange, "过滤器链未被调用");
            return exchange;
        }
    }
}
