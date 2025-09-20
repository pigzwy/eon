package com.eon.common.security.context;

import com.eon.common.security.constant.AuthHeaderConstants;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAuthContextExtractorTest {

    @Test
    void extract_should_build_user_from_gateway_headers() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthHeaderConstants.HDR_X_USER_ID, "1001");
        request.addHeader(AuthHeaderConstants.HDR_X_TENANT_ID, "2001");
        request.addHeader(AuthHeaderConstants.HDR_X_POLICY_VERSION, "3");
        request.addHeader(AuthHeaderConstants.HDR_X_ROLES, "ADMIN,USER");
        request.addHeader(AuthHeaderConstants.HDR_X_USER_PERMISSIONS, "user:read,user:update");
        request.addHeader(AuthHeaderConstants.HDR_X_TRACE_ID, "trace-123");

        AuthenticatedUser user = GatewayAuthContextExtractor.extract(request).orElseThrow();

        assertEquals(1001L, user.userId());
        assertEquals(2001L, user.tenantId());
        assertEquals(3, user.policyVersion());
        assertEquals(Set.of("ADMIN", "USER"), user.roles());
        assertEquals(Set.of("user:read", "user:update"), user.permissions());
        assertEquals("trace-123", user.traceId());
    }

    @Test
    void extract_should_return_empty_when_header_missing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertTrue(GatewayAuthContextExtractor.extract(request).isEmpty());
    }
}
