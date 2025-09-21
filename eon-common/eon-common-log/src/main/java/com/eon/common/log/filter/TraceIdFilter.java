package com.eon.common.log.filter;

import com.eon.common.log.properties.TraceIdProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 简单的链路追踪过滤器：优先使用上游 traceId，没有时按需生成并写回响应头。
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final TraceIdProperties properties;
    private final Supplier<String> traceIdGenerator;

    public TraceIdFilter(TraceIdProperties properties) {
        this(properties, () -> UUID.randomUUID().toString().replaceAll("-", ""));
    }

    TraceIdFilter(TraceIdProperties properties, Supplier<String> traceIdGenerator) {
        this.properties = properties;
        this.traceIdGenerator = traceIdGenerator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = resolveTraceId(request);
        if (StringUtils.hasText(traceId)) {
            MDC.put(properties.getMdcKey(), traceId);
            response.setHeader(properties.getHeaderName(), traceId);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(properties.getMdcKey());
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(properties.getHeaderName());
        if (StringUtils.hasText(incoming)) {
            return incoming;
        }
        if (properties.isGenerateIfMissing()) {
            return Optional.ofNullable(traceIdGenerator.get()).filter(StringUtils::hasText).orElse(null);
        }
        return null;
    }
}
