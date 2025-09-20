package com.eon.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 网关全局配置：集中定义跨域、WebClient 负载均衡与 JWT 解码器。
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties({GatewaySecurityProperties.class, GatewayCorsProperties.class})
public class GatewayConfiguration {

    private static final List<String> BUILTIN_WHITELIST = List.of(
            "/.well-known/openid-configuration",
            "/oauth2/jwks",
            "/oauth2/token",
            "/oauth2/introspect"
    );

    /**
     * CORS 过滤器：根据 `gateway.cors` 配置动态控制跨域策略。
     * 默认仅暴露认证头部，生产环境请按需收紧来源列表。
     */
    @Bean
    public CorsWebFilter corsWebFilter(GatewayCorsProperties props) {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(props.allowedOrigins());
        configuration.setAllowedMethods(props.allowedMethods());
        configuration.setAllowedHeaders(props.allowedHeaders());
        configuration.addExposedHeader(HttpHeaders.AUTHORIZATION);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Spring Cloud Gateway 会为所有路由复用该配置
        source.registerCorsConfiguration("/**", configuration);
        return new CorsWebFilter(source);
    }

    /**
     * Spring Security 过滤链：统一控制白名单与 OAuth2 资源服务器能力。
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, GatewaySecurityProperties props) {
        Set<String> whitelist = new LinkedHashSet<>(BUILTIN_WHITELIST);
        whitelist.addAll(props.getWhitelist());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // 预检请求直接放行，避免 CORS 被误拦截
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(whitelist.toArray(String[]::new)).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(GatewaySecurityProperties props, WebClient.Builder builder) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(props.getJwksUri())
                .webClient(builder.build())
                .build();

        if (props.getIssuer() != null && !props.getIssuer().isBlank()) {
            decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getIssuer()));
        } else {
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator()));
        }
        return decoder;
    }
}
