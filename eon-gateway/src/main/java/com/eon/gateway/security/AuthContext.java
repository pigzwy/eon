package com.eon.gateway.security;

import java.util.Collections;
import java.util.Set;

/**
 * 认证后的用户上下文（存放于 exchange attribute）。
 */
public class AuthContext {
    private final String userId;
    private final Set<String> roles;
    private final String tenantId;

    public AuthContext(String userId, Set<String> roles, String tenantId) {
        this.userId = userId;
        this.roles = roles == null ? Collections.emptySet() : roles;
        this.tenantId = tenantId;
    }

    public String getUserId() { return userId; }
    public Set<String> getRoles() { return roles; }
    public String getTenantId() { return tenantId; }
}

