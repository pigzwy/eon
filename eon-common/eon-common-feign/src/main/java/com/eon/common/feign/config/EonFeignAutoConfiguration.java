package com.eon.common.feign.config;

import com.eon.common.feign.properties.EonFeignProperties;
import com.eon.common.feign.support.HeaderPropagationRequestInterceptor;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 为所有引入 `spring-cloud-starter-openfeign` 的服务提供统一配置。
 */
@AutoConfiguration
@ConditionalOnClass({RequestInterceptor.class, Logger.class})
@EnableConfigurationProperties(EonFeignProperties.class)
@ConditionalOnProperty(prefix = "eon.feign", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EonFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor eonHeaderPropagationInterceptor(EonFeignProperties properties) {
        return new HeaderPropagationRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level feignLoggerLevel(EonFeignProperties properties) {
        return properties.getLogLevel();
    }

    @Bean
    @ConditionalOnMissingBean(Request.Options.class)
    public Request.Options feignRequestOptions(EonFeignProperties properties) {
        Duration connectTimeout = properties.getConnectTimeout();
        Duration readTimeout = properties.getReadTimeout();
        return new Request.Options(connectTimeout, readTimeout, true);
    }
}
