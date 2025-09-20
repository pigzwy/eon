package com.eon.common.security.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 将 {@link CurrentUser} 注解的参数解析为 {@link AuthenticatedUser}。
 */
@Slf4j
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable org.springframework.web.bind.support.WebDataBinderFactory binderFactory) throws Exception {
        log.debug("CurrentUserArgumentResolver开始解析用户信息，方法: {}.{}", 
                parameter.getDeclaringClass().getSimpleName(), parameter.getMethod().getName());
        
        // 优先从ThreadLocal获取用户上下文
        AuthenticatedUser user = UserContextHolder.get();
        if (user != null) {
            log.debug("从ThreadLocal获取到用户信息: userId={}", user.userId());
            return user;
        }
        
        // 如果ThreadLocal为空，尝试从请求属性获取用户信息
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request != null) {
            AuthenticatedUser fromRequest = UserContextHolder.fromRequest(request);
            if (fromRequest != null) {
                log.debug("从Request属性获取到用户信息: userId={}", fromRequest.userId());
                return fromRequest;
            }
            log.warn("ThreadLocal和Request属性都未找到用户信息，请求头: X-User-Id={}, X-Tenant-Id={}", 
                    request.getHeader("X-User-Id"), request.getHeader("X-Tenant-Id"));
        } else {
            log.warn("无法获取HttpServletRequest对象");
        }
        
        // 如果都获取不到，抛出明确的认证异常
        throw new AuthenticationCredentialsNotFoundException(
                "用户未认证或认证信息缺失，请确保请求包含正确的认证头信息 (X-User-Id, X-Tenant-Id等)");
    }
}
