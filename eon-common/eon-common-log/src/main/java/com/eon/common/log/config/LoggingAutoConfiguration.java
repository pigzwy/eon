package com.eon.common.log.config;

import com.eon.common.log.filter.TraceIdFilter;
import com.eon.common.log.properties.TraceIdProperties;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 自动装配链路追踪过滤器，默认高优先级注册，便于在后续日志中读取 traceId。
 */
@AutoConfiguration
@ConditionalOnClass({OncePerRequestFilter.class, MDC.class})
@EnableConfigurationProperties(TraceIdProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean
    public TraceIdFilter traceIdFilter(TraceIdProperties properties) {
        return new TraceIdFilter(properties);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(name = "traceIdFilterRegistration")
    @ConditionalOnProperty(prefix = "eon.logging.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceIdFilter traceIdFilter, TraceIdProperties properties) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>(traceIdFilter);
        registrationBean.setName("eonTraceIdFilter");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setMatchAfter(false);
        registrationBean.getInitParameters().put("headerName", properties.getHeaderName());
        return registrationBean;
    }
}
