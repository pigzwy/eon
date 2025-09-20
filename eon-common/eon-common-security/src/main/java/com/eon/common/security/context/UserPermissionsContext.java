package com.eon.common.security.context;

import java.util.Collections;
import java.util.List;

/**
 * 请求级权限上下文，通过 ThreadLocal 保存当前请求的权限列表。
 */
public final class UserPermissionsContext {

    private static final ThreadLocal<List<String>> HOLDER = new ThreadLocal<>();

    private UserPermissionsContext() {
    }

    /**
     * 设置当前请求的权限列表，空值会被归一化为空集合。
     */
    public static void setPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            HOLDER.set(Collections.emptyList());
        } else {
            HOLDER.set(List.copyOf(permissions));
        }
    }

    /**
     * 获取当前请求的权限列表，未设置时返回空集合。
     */
    public static List<String> getPermissions() {
        List<String> permissions = HOLDER.get();
        return permissions == null ? Collections.emptyList() : permissions;
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏。
     */
    public static void clear() {
        HOLDER.remove();
    }
}
