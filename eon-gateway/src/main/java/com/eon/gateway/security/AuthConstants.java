package com.eon.gateway.security;

/**
 * 认证上下文常量与请求头定义。
 */
public interface AuthConstants {
    String ATTR_AUTH_CONTEXT = "com.eon.gateway.AUTH_CONTEXT";
    String ATTR_TRACE_ID = "com.eon.gateway.TRACE_ID";

    String HDR_AUTHORIZATION = "Authorization";
    String HDR_BEARER_PREFIX = "Bearer ";

    // 下游透传头（内部）
    String HDR_X_USER_ID = "X-User-Id";
    String HDR_X_ROLES = "X-Roles";
    String HDR_X_USER_ROLES = "X-User-Roles"; // 兼容旧版本
    String HDR_X_PERMISSIONS = "X-Permissions";
    String HDR_X_USER_PERMISSIONS = "X-User-Permissions"; // 兼容旧版本
    String HDR_X_TENANT_ID = "X-Tenant-Id";
    String HDR_X_POLICY_VERSION = "X-Policy-Version";
    String HDR_X_TRACE_ID = "X-Trace-Id";
}

