package com.eon.gateway.support;

import com.eon.gateway.filter.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * 限流 KeyResolver：优先使用登录用户 ID，没有登录态时基于真实客户端 IP 进行限流。
 * Bean 名称保持为 userKeyResolver，方便与 RedisRateLimiter 等默认配置对接。
 */
@Component("userKeyResolver")
public class UserKeyResolver implements KeyResolver {

    private static final RemoteAddressResolver REMOTE_ADDRESS_RESOLVER =
            XForwardedRemoteAddressResolver.maxTrustedIndex(1);

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        Object userId = exchange.getAttribute(JwtAuthenticationFilter.ATTR_USER_ID);
        if (userId != null) {
            return Mono.just("user:" + userId);
        }
        // 兼容经由负载均衡或代理的场景，优先解析 X-Forwarded-For
        InetSocketAddress address = REMOTE_ADDRESS_RESOLVER.resolve(exchange);
        String ip = address == null || address.getAddress() == null
                ? "anonymous"
                : address.getAddress().getHostAddress();
        return Mono.just("ip:" + ip);
    }
}
