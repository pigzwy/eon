package com.eon.common.security.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 将网关透传的权限头信息写入 {@link UserPermissionsContext}，供业务层读取。
 */
public class UserPermissionsInterceptor implements HandlerInterceptor {

    private static final String HEADER_PERMISSIONS = "X-User-Permissions";
    private static final String ATTRIBUTE_PERMISSIONS = "userPermissions";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        List<String> permissions = resolvePermissions(request.getHeader(HEADER_PERMISSIONS));
        UserPermissionsContext.setPermissions(permissions);
        request.setAttribute(ATTRIBUTE_PERMISSIONS, permissions);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserPermissionsContext.clear();
    }

    private List<String> resolvePermissions(String header) {
        if (header == null || header.isBlank()) {
            return List.of();
        }
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
    }
}
