package com.eon.common.feign.support;

import com.eon.common.feign.properties.EonFeignProperties;
import com.eon.common.security.constant.AuthHeaderConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 将当前请求中的认证、租户、Trace 等关键信息透传至下游 Feign 请求。
 */
public class HeaderPropagationRequestInterceptor implements RequestInterceptor {

    private final EonFeignProperties properties;

    public HeaderPropagationRequestInterceptor(EonFeignProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isEnabled()) {
            return;
        }

        appendStaticHeaders(template);
        if (!properties.isHeaderPropagationEnabled()) {
            return;
        }

        Set<String> headers = new LinkedHashSet<>(properties.getHeaders());
        HttpServletRequest request = extractCurrentRequest();
        if (request != null) {
            propagateFromRequest(template, headers, request);
            return;
        }

        if (properties.isFallbackMdcTraceId()) {
            String traceId = MDC.get(properties.getMdcTraceIdKey());
            if (StringUtils.hasText(traceId) && headers.contains(AuthHeaderConstants.HDR_X_TRACE_ID)) {
                template.header(AuthHeaderConstants.HDR_X_TRACE_ID, traceId);
            }
        }
    }

    private void appendStaticHeaders(RequestTemplate template) {
        for (Map.Entry<String, String> entry : properties.getStaticHeaders().entrySet()) {
            if (StringUtils.hasText(entry.getKey()) && StringUtils.hasText(entry.getValue())) {
                template.header(entry.getKey(), entry.getValue());
            }
        }
    }

    private HttpServletRequest extractCurrentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private void propagateFromRequest(RequestTemplate template, Collection<String> headers, HttpServletRequest request) {
        if (CollectionUtils.isEmpty(headers)) {
            return;
        }
        for (String header : headers) {
            if (!StringUtils.hasText(header)) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(header);
            if (values == null || !values.hasMoreElements()) {
                continue;
            }
            template.header(header, java.util.Collections.list(values));
        }
    }
}
