package com.eon.gateway.filter;

import com.eon.gateway.security.AuthConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

/**
 * 请求日志过滤器（简单采样，默认全部记录，可改为概率采样）。
 */
@Component
public class RequestLogFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public int getOrder() {
        // 100 保证在鉴权、限流之后执行，仅负责记录最终结果
        return 100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String traceId = (String) exchange.getAttribute(AuthConstants.ATTR_TRACE_ID);
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod httpMethod = exchange.getRequest().getMethod();
        // 某些异常请求可能不存在方法，此处兜底为 UNKNOWN 方便排查
        String method = httpMethod == null ? "UNKNOWN" : httpMethod.name();
        log.info("gw.req start traceId={} {} {}", traceId, method, path);
        long start = System.currentTimeMillis();
        return chain.filter(exchange).doOnTerminate(() -> {
            long cost = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() == null ? 0 : exchange.getResponse().getStatusCode().value();
            log.info("gw.req end   traceId={} status={} cost={}ms", traceId, status, cost);
        });
    }
}
