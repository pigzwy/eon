package com.eon.common.security.context;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 当前请求内的用户身份快照。
 */
public record AuthenticatedUser(
        Long userId,
        Long tenantId,
        Integer policyVersion,
        Set<String> roles,
        Set<String> permissions,
        String traceId
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticatedUser {
        roles = normalise(roles);
        permissions = normalise(permissions);
    }

    private static Set<String> normalise(Set<String> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptySet();
        }
        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String value : source) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return Collections.unmodifiableSet(cleaned);
    }

    public boolean hasRole(String role) {
        return role != null && roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permission != null && permissions.contains(permission);
    }

    public boolean isSameUser(Long anotherUserId) {
        return Objects.equals(this.userId, anotherUserId);
    }
}
