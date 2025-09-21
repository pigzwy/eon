package com.eon.common.log.filter;

import com.eon.common.log.properties.TraceIdProperties;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdFilterTest {

    @Test
    void shouldReuseIncomingTraceId() throws ServletException, IOException {
        TraceIdProperties properties = new TraceIdProperties();
        TraceIdFilter filter = new TraceIdFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(properties.getHeaderName(), "trace-10086");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(properties.getHeaderName())).isEqualTo("trace-10086");
        assertThat(MDC.get(properties.getMdcKey())).isNull();
    }

    @Test
    void shouldGenerateTraceIdWhenMissing() throws ServletException, IOException {
        TraceIdProperties properties = new TraceIdProperties();
        properties.setGenerateIfMissing(true);
        TraceIdFilter filter = new TraceIdFilter(properties);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String header = response.getHeader(properties.getHeaderName());
        assertThat(header).isNotBlank();
        assertThat(header).hasSize(32); // UUID 去掉连字符后的默认长度
        assertThat(MDC.get(properties.getMdcKey())).isNull();
    }
}
