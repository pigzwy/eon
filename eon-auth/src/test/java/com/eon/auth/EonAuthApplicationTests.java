package com.eon.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EonAuthApplicationTests {

    @Test
    void contextLoads() {
        // 测试应用上下文加载
    }

    // TODO: 添加OAuth2依赖后取消注释以下测试
    /*
    @Test
    void testPasswordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println("Password matches: " + encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testOAuth2ClientRegistration() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("test-client")
            .clientSecret(encoder.encode("test-secret"))
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://localhost:8080/authorized")
            .scope("read")
            .scope("write")
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(1))
                .reuseRefreshTokens(false)
                .build())
            .build();
        
        System.out.println("Client ID: " + client.getClientId());
        System.out.println("Client Secret: " + client.getClientSecret());
        System.out.println("Scopes: " + client.getScopes());
        System.out.println("Grant Types: " + client.getAuthorizationGrantTypes());
    }
    */
}