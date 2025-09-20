package com.eon.common.security.context;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 自动装配：注入过滤器与参数解析器，开箱即用。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GatewayAuthContextFilter gatewayAuthContextFilter() {
        return new GatewayAuthContextFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "gatewayAuthContextFilterRegistration")
    public FilterRegistrationBean<GatewayAuthContextFilter> gatewayAuthContextFilterRegistration(GatewayAuthContextFilter filter) {
        FilterRegistrationBean<GatewayAuthContextFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return bean;
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserArgumentResolver currentUserArgumentResolver() {
        return new CurrentUserArgumentResolver();
    }

    @Bean
    @ConditionalOnMissingBean(name = "currentUserWebMvcConfigurer")
    public WebMvcConfigurer currentUserWebMvcConfigurer(CurrentUserArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<org.springframework.web.method.support.HandlerMethodArgumentResolver> resolvers) {
                // 直接在自动装配阶段注册 @CurrentUser 解析器，避免业务侧自定义 WebMvcConfigurer 时丢失能力
                resolvers.add(resolver);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public UserPermissionsInterceptor userPermissionsInterceptor() {
        return new UserPermissionsInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(name = "userPermissionsWebMvcConfigurer")
    public WebMvcConfigurer userPermissionsWebMvcConfigurer(UserPermissionsInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 自动注册权限拦截器，让权限头透传至业务 ThreadLocal
                registry.addInterceptor(interceptor).addPathPatterns("/**");
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        // 提供默认的 BCrypt 实现，业务如需替换可自定义 Bean 覆盖
        return new BCryptPasswordEncoder();
    }
}
