package com.eon.common.log.filter;

import com.eon.common.log.properties.TraceIdProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 针对 {@link TraceIdFilter} 的最小行为验证，作为覆盖率基线示例。
 */
class TraceIdFilterTest {

    @Test
    void 应复用上游传入的TraceId() throws ServletException, IOException {
        TraceIdProperties properties = new TraceIdProperties();
        properties.setHeaderName("X-Trace-Id");
        properties.setMdcKey("traceId");

        TraceIdFilter filter = new TraceIdFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, captureTrace(traceInsideChain));

        assertThat(traceInsideChain.get()).isEqualTo("trace-123");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("trace-123");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void 应在缺失TraceId时按需生成() throws ServletException, IOException {
        TraceIdProperties properties = new TraceIdProperties();
        properties.setHeaderName("X-Trace-Id");
        properties.setMdcKey("traceId");
        properties.setGenerateIfMissing(true);

        TraceIdFilter filter = new TraceIdFilter(properties, () -> "generated-999");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> traceInsideChain = new AtomicReference<>();

        filter.doFilter(request, response, captureTrace(traceInsideChain));

        assertThat(traceInsideChain.get()).isEqualTo("generated-999");
        assertThat(response.getHeader("X-Trace-Id")).isEqualTo("generated-999");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void 关闭过滤器时应直接透传() throws ServletException, IOException {
        TraceIdProperties properties = new TraceIdProperties();
        properties.setEnabled(false);

        TraceIdFilter filter = new TraceIdFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (ServletRequest req, ServletResponse res) -> chainInvoked.set(true));

        assertThat(chainInvoked).isTrue();
        assertThat(response.getHeaderNames()).isEmpty();
    }

    private jakarta.servlet.FilterChain captureTrace(AtomicReference<String> traceInsideChain) {
        return (ServletRequest request, ServletResponse response) -> traceInsideChain.set(MDC.get("traceId"));
    }
}
