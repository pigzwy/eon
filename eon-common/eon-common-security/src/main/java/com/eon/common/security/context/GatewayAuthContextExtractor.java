package com.eon.common.security.context;

import com.eon.common.security.constant.AuthHeaderConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 从网关透传头解析用户身份。
 */
public final class GatewayAuthContextExtractor {

    private GatewayAuthContextExtractor() {
    }

    public static Optional<AuthenticatedUser> extract(HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }
        String userIdHeader = request.getHeader(AuthHeaderConstants.HDR_X_USER_ID);
        if (!StringUtils.hasText(userIdHeader)) {
            return Optional.empty();
        }
        Long userId = parseLong(userIdHeader);
        if (userId == null) {
            return Optional.empty();
        }
        Long tenantId = parseLong(request.getHeader(AuthHeaderConstants.HDR_X_TENANT_ID));
        Integer policyVersion = parseInteger(request.getHeader(AuthHeaderConstants.HDR_X_POLICY_VERSION));
        String traceId = request.getHeader(AuthHeaderConstants.HDR_X_TRACE_ID);

        Set<String> roles = mergeMultiValueHeaders(request,
                AuthHeaderConstants.HDR_X_ROLES,
                AuthHeaderConstants.HDR_X_USER_ROLES);
        Set<String> permissions = mergeMultiValueHeaders(request,
                AuthHeaderConstants.HDR_X_PERMISSIONS,
                AuthHeaderConstants.HDR_X_USER_PERMISSIONS);

        AuthenticatedUser user = new AuthenticatedUser(
                userId,
                tenantId,
                policyVersion,
                roles,
                permissions,
                StringUtils.hasText(traceId) ? traceId : null
        );
        return Optional.of(user);
    }

    private static Set<String> mergeMultiValueHeaders(HttpServletRequest request, String... names) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String name : names) {
            String header = request.getHeader(name);
            if (!StringUtils.hasText(header)) {
                continue;
            }
            String[] parts = header.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        }
        return values;
    }

    private static Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
