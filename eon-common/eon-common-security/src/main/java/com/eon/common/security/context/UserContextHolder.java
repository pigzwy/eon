package com.eon.common.security.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 请求级用户上下文持有器，封装 ThreadLocal 访问。
 */
public final class UserContextHolder {

    public static final String ATTR_CURRENT_USER = UserContextHolder.class.getName() + ".CURRENT_USER";

    private static final ThreadLocal<AuthenticatedUser> CONTEXT = new InheritableThreadLocal<>();

    private UserContextHolder() {
    }

    public static void set(AuthenticatedUser user) {
        if (user == null) {
            CONTEXT.remove();
        } else {
            CONTEXT.set(user);
        }
    }

    public static AuthenticatedUser get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static AuthenticatedUser fromRequest(HttpServletRequest request) {
        Object attr = request.getAttribute(ATTR_CURRENT_USER);
        if (attr instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }
}
