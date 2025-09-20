package com.eon.common.security.constant;

/**
 * 统一的认证头定义，便于各服务解耦网关实现细节。
 */
public interface AuthHeaderConstants {

    String HDR_AUTHORIZATION = "Authorization";
    String HDR_X_USER_ID = "X-User-Id";
    String HDR_X_TENANT_ID = "X-Tenant-Id";
    String HDR_X_POLICY_VERSION = "X-Policy-Version";
    String HDR_X_ROLES = "X-Roles";
    String HDR_X_USER_ROLES = "X-User-Roles";
    String HDR_X_PERMISSIONS = "X-Permissions";
    String HDR_X_USER_PERMISSIONS = "X-User-Permissions";
    String HDR_X_TRACE_ID = "X-Trace-Id";
}
