package com.eon.auth.config;

import com.eon.auth.support.user.UserAuthorityService;
import com.eon.auth.support.user.UserAuthorityService.UserAuthoritySnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 JWT 自定义器能够根据用户权限快照写入所需声明。
 */
class FederatedIdentityIdTokenCustomizerTest {

    @Test
    void customize_should_write_rbac_claims_into_access_token() {
        UserAuthorityService authorityService = Mockito.mock(UserAuthorityService.class);
        FederatedIdentityIdTokenCustomizer customizer = new FederatedIdentityIdTokenCustomizer(
                authorityService,
                List.of("eon-internal")
        );

        TestingAuthenticationToken principal = new TestingAuthenticationToken("alice", "password");
        UserAuthoritySnapshot snapshot = new UserAuthoritySnapshot(
                1001L,
                2002L,
                7,
                List.of("ADMIN", "AUDITOR"),
                List.of("user:read", "user:export")
        );
        Mockito.when(authorityService.loadAuthoritySnapshot("alice")).thenReturn(snapshot);

        JwtEncodingContext context = JwtEncodingContext.with(
                        JwsHeader.with(SignatureAlgorithm.RS256),
                        JwtClaimsSet.builder()
                )
                .principal(principal)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        customizer.customize(context);

        JwtClaimsSet claims = context.getClaims().build();
        assertEquals(List.of("eon-internal"), claims.getAudience());
        assertEquals(Long.valueOf(1001L), claims.getClaim("uid"));
        assertEquals(Long.valueOf(2002L), claims.getClaim("tenant"));
        assertEquals(Integer.valueOf(7), claims.getClaim("pv"));
        assertEquals(List.of("ADMIN", "AUDITOR"), claims.getClaim("roles"));
        assertEquals(List.of("user:read", "user:export"), claims.getClaim("permissions"));

        @SuppressWarnings("unchecked")
        List<String> authorities = (List<String>) claims.getClaim("authorities");
        assertEquals(List.of("ROLE_ADMIN", "ROLE_AUDITOR", "user:read", "user:export"), authorities);
    }

    @Test
    void customize_should_skip_when_user_not_found() {
        UserAuthorityService authorityService = Mockito.mock(UserAuthorityService.class);
        FederatedIdentityIdTokenCustomizer customizer = new FederatedIdentityIdTokenCustomizer(
                authorityService,
                List.of()
        );

        TestingAuthenticationToken principal = new TestingAuthenticationToken("bob", "password");
        Mockito.when(authorityService.loadAuthoritySnapshot("bob"))
                .thenReturn(UserAuthoritySnapshot.EMPTY);

        JwtEncodingContext context = JwtEncodingContext.with(
                        JwsHeader.with(SignatureAlgorithm.RS256),
                        JwtClaimsSet.builder().claim("sentinel", true)
                )
                .principal(principal)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        customizer.customize(context);

        JwtClaimsSet claims = context.getClaims().build();
        assertEquals(Boolean.TRUE, claims.getClaim("sentinel"));
        assertEquals(1, claims.getClaims().size(), "未找到用户时不应追加额外声明");
    }
}
