package com.eon.user.web;

import com.eon.common.security.context.UserPermissionsContext;
import com.eon.common.security.context.UserPermissionsInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证权限拦截器能够正确解析头信息并写入上下文。
 */
class UserPermissionsInterceptorTest {

    private final UserPermissionsInterceptor interceptor = new UserPermissionsInterceptor();

    @AfterEach
    void tearDown() {
        UserPermissionsContext.clear();
    }

    @Test
    void preHandle_should_extract_permissions_and_context() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Permissions", "user:read , order:write ,, ");

        boolean proceed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertTrue(proceed, "拦截器应继续后续处理");

        assertEquals(List.of("user:read", "order:write"), UserPermissionsContext.getPermissions());
        @SuppressWarnings("unchecked")
        List<String> attr = (List<String>) request.getAttribute("userPermissions");
        assertEquals(List.of("user:read", "order:write"), attr);

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);
        assertTrue(UserPermissionsContext.getPermissions().isEmpty(), "完成后应清理上下文");
    }

    @Test
    void preHandle_should_handle_missing_header() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(UserPermissionsContext.getPermissions().isEmpty(), "未携带头时权限应为空");
        @SuppressWarnings("unchecked")
        List<String> attr = (List<String>) request.getAttribute("userPermissions");
        assertEquals(List.of(), attr);
    }
}
