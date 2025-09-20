package com.eon.gateway.filter;

import com.eon.gateway.config.GatewaySecurityProperties;
import com.eon.gateway.security.AuthConstants;
import com.eon.gateway.security.AuthContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器：网关的第一道安全防线，负责解析JWT令牌并提取用户信息
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>JWT验证</b>：验证JWT令牌的签名和有效期，确保令牌合法性</li>
 *   <li><b>用户信息提取</b>：从JWT中提取用户ID、角色、租户等关键信息</li>
 *   <li><b>请求上下文设置</b>：将用户信息存入请求属性，供后续过滤器使用</li>
 *   <li><b>请求头增强</b>：向后端服务转发用户信息，支持业务逻辑</li>
 *   <li><b>安全拦截</b>：拦截无效或过期的令牌，保护后端服务</li>
 * </ul>
 * 
 * <p>执行时机：</p>
 * <ul>
 *   <li>执行顺序：-10（较高优先级，在AuthorizationFilter之前执行）</li>
 *   <li>适用范围：所有非白名单的API请求</li>
 *   <li>拦截位置：网关层，请求到达后端服务之前</li>
 * </ul>
 * 
 * <p>安全特性：</p>
 * <ul>
 *   <li><b>无状态认证</b>：基于JWT标准，支持分布式部署</li>
 *   <li><b>白名单机制</b>：支持路径白名单，保护公开接口</li>
 *   <li><b>错误处理</b>：统一的错误响应格式，避免系统信息泄露</li>
 *   <li><b>日志记录</b>：记录认证失败事件，支持安全审计</li>
 * </ul>
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /** 请求属性常量：用户ID，用于在过滤器之间传递用户信息 */
    public static final String ATTR_USER_ID = "gateway.userId";
    
    /** 请求属性常量：用户角色列表，用于权限鉴权 */
    public static final String ATTR_ROLES = "gateway.roles";
    /** 请求属性常量：用户权限列表，用于细粒度鉴权 */
    public static final String ATTR_PERMISSIONS = "gateway.permissions";

    /** 需要统一清理的敏感透传头，避免客户端伪造 */
    private static final List<String> SENSITIVE_FORWARD_HEADERS = List.of(
            AuthConstants.HDR_X_USER_ID,
            AuthConstants.HDR_X_TENANT_ID,
            AuthConstants.HDR_X_POLICY_VERSION,
            AuthConstants.HDR_X_ROLES,
            AuthConstants.HDR_X_USER_ROLES,
            AuthConstants.HDR_X_PERMISSIONS,
            AuthConstants.HDR_X_USER_PERMISSIONS
    );

    /** 日志记录器：记录认证过程中的重要事件和错误信息 */
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    /** 路径匹配器：用于白名单路径匹配，支持Ant风格路径模式 */
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 网关安全配置属性：包含白名单路径等安全配置 */
    private final GatewaySecurityProperties securityProperties;
    
    /** JWT解码器：用于验证和解析JWT令牌 */
    private final ReactiveJwtDecoder jwtDecoder;

    /**
     * 构造函数：注入JWT认证所需的组件
     * 
     * @param securityProperties 网关安全配置属性
     * @param jwtDecoder JWT令牌解码器
     */
    public JwtAuthenticationFilter(GatewaySecurityProperties securityProperties,
                                   ReactiveJwtDecoder jwtDecoder) {
        this.securityProperties = securityProperties;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(AuthConstants.HDR_AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(AuthConstants.HDR_BEARER_PREFIX)) {
            return Mono.defer(() -> unauthorized(exchange, "缺少认证信息"));
        }
        String token = authorization.substring(AuthConstants.HDR_BEARER_PREFIX.length());
        return jwtDecoder.decode(token)
                .flatMap(jwt -> handleSuccess(exchange, chain, jwt))
                .onErrorResume(ex -> {
                    if (ex instanceof JwtException || ex instanceof IllegalArgumentException) {
                        log.warn("JWT 验证失败: {}", ex.getMessage(), ex);
                        return Mono.defer(() -> unauthorized(exchange, "令牌无效或已过期"));
                    }
                    return Mono.error(ex);
                });
    }

    private Mono<Void> handleSuccess(ServerWebExchange exchange, GatewayFilterChain chain, Jwt jwt) {
        String userId = stringClaim(jwt, "uid");
        if (!StringUtils.hasText(userId)) {
            return Mono.defer(() -> unauthorized(exchange, "令牌缺少 uid 声明"));
        }

        String expectedIssuer = securityProperties.getIssuer();
        if (StringUtils.hasText(expectedIssuer)) {
            String tokenIssuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
            if (!expectedIssuer.equals(tokenIssuer)) {
                log.warn("JWT issuer mismatch, expected={}, actual={}", expectedIssuer, tokenIssuer);
                return Mono.defer(() -> unauthorized(exchange, "令牌发行方不受信任"));
            }
        }

        String expectedAudience = securityProperties.getAudience();
        if (StringUtils.hasText(expectedAudience)) {
            List<String> audiences = jwt.getAudience();
            boolean matched = audiences != null && audiences.stream().anyMatch(expectedAudience::equals);
            if (!matched) {
                log.warn("JWT audience mismatch, expected={}, actual={}", expectedAudience, audiences);
                return Mono.defer(() -> unauthorized(exchange, "令牌受众不匹配"));
            }
        }

        String tenant = stringClaim(jwt, "tenant");
        String policyVersion = stringClaim(jwt, "pv");
        List<String> roles = rolesClaim(jwt);
        List<String> permissions = permissionsClaim(jwt);

        exchange.getAttributes().put(ATTR_USER_ID, userId);
        exchange.getAttributes().put(ATTR_ROLES, roles);
        exchange.getAttributes().put(ATTR_PERMISSIONS, permissions);

        AuthContext context = new AuthContext(userId, new LinkedHashSet<>(roles), tenant);
        exchange.getAttributes().put(AuthConstants.ATTR_AUTH_CONTEXT, context);

        String traceId = exchange.getAttribute(AuthConstants.ATTR_TRACE_ID);

        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.headers(headers -> {
                    SENSITIVE_FORWARD_HEADERS.forEach(headers::remove);
                    headers.set(AuthConstants.HDR_X_USER_ID, userId);
                    if (StringUtils.hasText(tenant)) {
                        headers.set(AuthConstants.HDR_X_TENANT_ID, tenant);
                    }
                    if (StringUtils.hasText(policyVersion)) {
                        headers.set(AuthConstants.HDR_X_POLICY_VERSION, policyVersion);
                    }
                    if (!roles.isEmpty()) {
                        String joinedRoles = String.join(",", roles);
                        headers.set(AuthConstants.HDR_X_ROLES, joinedRoles);
                        headers.set(AuthConstants.HDR_X_USER_ROLES, joinedRoles);
                    }
                    if (!permissions.isEmpty()) {
                        String joinedPermissions = String.join(",", permissions);
                        headers.set(AuthConstants.HDR_X_PERMISSIONS, joinedPermissions);
                        headers.set(AuthConstants.HDR_X_USER_PERMISSIONS, joinedPermissions);
                    }
                    if (StringUtils.hasText(traceId)) {
                        headers.set(AuthConstants.HDR_X_TRACE_ID, traceId);
                    }
                }))
                .build();
        return chain.filter(mutated);
    }

    private boolean isWhitelisted(String path) {
        List<String> patterns = securityProperties.getWhitelist();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String stringClaim(Jwt jwt, String name) {
        Object value = jwt.getClaims().get(name);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<String> rolesClaim(Jwt jwt) {
        Set<String> collected = new LinkedHashSet<>();
        Object raw = jwt.getClaims().get("roles");
        if (raw instanceof List<?> list) {
            list.stream().map(String::valueOf).forEach(collected::add);
        } else if (raw instanceof String str) {
            if (!str.isBlank()) {
                collected.add(str);
            }
        }
        Object scope = jwt.getClaims().get("scope");
        if (scope instanceof String scopeStr) {
            Arrays.stream(scopeStr.split(" "))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(collected::add);
        }
        return List.copyOf(collected);
    }

    @SuppressWarnings("unchecked")
    private List<String> permissionsClaim(Jwt jwt) {
        Object raw = jwt.getClaims().get("permissions");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        if (raw instanceof String str) {
            return Arrays.stream(str.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        Object authorities = jwt.getClaims().get("authorities");
        if (authorities instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .filter(value -> value.contains(":"))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.empty();
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"code\":\"UNAUTHORIZED\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
