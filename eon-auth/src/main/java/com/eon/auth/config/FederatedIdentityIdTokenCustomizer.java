package com.eon.auth.config;

import com.eon.auth.support.user.UserAuthorityService;
import com.eon.auth.support.user.UserAuthorityService.UserAuthoritySnapshot;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JWT 扩展钩子：在所有用户态 JWT（访问令牌、ID Token）中写入角色与权限声明。
 */
public class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final UserAuthorityService userAuthorityService;
    private final List<String> accessTokenAudiences;

    public FederatedIdentityIdTokenCustomizer(UserAuthorityService userAuthorityService,
                                              List<String> accessTokenAudiences) {
        this.userAuthorityService = userAuthorityService;
        if (accessTokenAudiences == null || accessTokenAudiences.isEmpty()) {
            this.accessTokenAudiences = List.of();
        } else {
            LinkedHashSet<String> cleaned = new LinkedHashSet<>();
            for (String value : accessTokenAudiences) {
                if (value == null) {
                    continue;
                }
                String trimmed = value.trim();
                if (!trimmed.isEmpty()) {
                    cleaned.add(trimmed);
                }
            }
            this.accessTokenAudiences = List.copyOf(cleaned);
        }
    }

    @Override
    public void customize(JwtEncodingContext context) {
        boolean isAccessToken = OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType());
        boolean isIdToken = OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue());
        if (!isAccessToken && !isIdToken) {
            // 只扩展 JWT 令牌（访问令牌与 ID Token），其他类型无需处理
            return;
        }

        Authentication principal = context.getPrincipal();
        if (principal == null) {
            // 客户端凭证等场景不会携带用户主体，直接跳过
            return;
        }

        if (isAccessToken && !accessTokenAudiences.isEmpty()) {
            context.getClaims().audience(accessTokenAudiences);
        }

        UserAuthoritySnapshot snapshot = userAuthorityService.loadAuthoritySnapshot(principal.getName());
        if (!snapshot.hasUser()) {
            // 若用户不存在或被禁用，说明此次令牌不是为某个业务用户签发，不追加任何声明
            return;
        }

        List<String> roleCodes = snapshot.roles();
        List<String> permissionCodes = snapshot.permissions();

        // authorities 字段保留 ROLE_* 与权限串的合集，方便资源服务一次性读取
        Set<String> mergedAuthorities = new LinkedHashSet<>();
        roleCodes.forEach(role -> mergedAuthorities.add("ROLE_" + role));
        mergedAuthorities.addAll(permissionCodes);

        context.getClaims().claim("uid", snapshot.userId());
        if (snapshot.tenantId() != null) {
            context.getClaims().claim("tenant", snapshot.tenantId());
        }
        if (snapshot.policyVersion() != null) {
            context.getClaims().claim("pv", snapshot.policyVersion());
        }
        context.getClaims().claim("roles", roleCodes);                // 角色列表，示例：["ADMIN","USER"]
        context.getClaims().claim("permissions", permissionCodes);    // 权限列表，示例：["/api/users:READ"]
        context.getClaims().claim("authorities", new ArrayList<>(mergedAuthorities));
    }
}
