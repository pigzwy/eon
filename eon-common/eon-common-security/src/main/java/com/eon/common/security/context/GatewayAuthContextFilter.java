package com.eon.common.security.context;

import com.eon.common.security.constant.AuthHeaderConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 网关头解析过滤器，确保后续链路能够读取统一的用户上下文。
 */
@Slf4j
public class GatewayAuthContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // 调试日志：输出请求头信息
            String userId = request.getHeader(AuthHeaderConstants.HDR_X_USER_ID);
            String tenantId = request.getHeader(AuthHeaderConstants.HDR_X_TENANT_ID);
            log.debug("GatewayAuthContextFilter执行中，请求路径: {}, X-User-Id: {}, X-Tenant-Id: {}", 
                    request.getRequestURI(), userId, tenantId);
            
            GatewayAuthContextExtractor.extract(request).ifPresent(user -> {
                log.debug("成功解析用户信息: userId={}, tenantId={}, roles={}, permissions={}", 
                        user.userId(), user.tenantId(), user.roles(), user.permissions());
                UserContextHolder.set(user);
                request.setAttribute(UserContextHolder.ATTR_CURRENT_USER, user);
            });
            
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
