package com.eon.common.feign.support;

import com.eon.common.feign.properties.EonFeignProperties;
import com.eon.common.security.constant.AuthHeaderConstants;
import feign.RequestTemplate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证请求头透传与静态头附加逻辑。
 */
class HeaderPropagationRequestInterceptorTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void 应透传请求头并附加静态头() {
        EonFeignProperties properties = new EonFeignProperties();
        properties.setHeaders(List.of(AuthHeaderConstants.HDR_AUTHORIZATION, AuthHeaderConstants.HDR_X_TRACE_ID));
        properties.setStaticHeaders(Map.of("X-Static", "static-value"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthHeaderConstants.HDR_AUTHORIZATION, "Bearer token-123");
        request.addHeader(AuthHeaderConstants.HDR_X_TRACE_ID, "trace-abc");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        HeaderPropagationRequestInterceptor interceptor = new HeaderPropagationRequestInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get(AuthHeaderConstants.HDR_AUTHORIZATION)).containsExactly("Bearer token-123");
        assertThat(template.headers().get(AuthHeaderConstants.HDR_X_TRACE_ID)).containsExactly("trace-abc");
        assertThat(template.headers().get("X-Static")).containsExactly("static-value");
    }

    @Test
    void 应从MDC兜底TraceId() {
        EonFeignProperties properties = new EonFeignProperties();
        properties.setHeaders(List.of(AuthHeaderConstants.HDR_X_TRACE_ID));
        properties.setFallbackMdcTraceId(true);
        properties.setMdcTraceIdKey("traceId");
        MDC.put("traceId", "mdc-trace-1");

        HeaderPropagationRequestInterceptor interceptor = new HeaderPropagationRequestInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers().get(AuthHeaderConstants.HDR_X_TRACE_ID)).containsExactly("mdc-trace-1");
    }

    @Test
    void 禁用透传时仅附加静态头() {
        EonFeignProperties properties = new EonFeignProperties();
        properties.setHeaderPropagationEnabled(false);
        properties.setStaticHeaders(Map.of("X-App", "demo"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthHeaderConstants.HDR_X_TRACE_ID, "trace-should-not-pass");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        HeaderPropagationRequestInterceptor interceptor = new HeaderPropagationRequestInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertThat(template.headers()).containsEntry("X-App", List.of("demo"));
        assertThat(template.headers()).doesNotContainKey(AuthHeaderConstants.HDR_X_TRACE_ID);
    }
}
