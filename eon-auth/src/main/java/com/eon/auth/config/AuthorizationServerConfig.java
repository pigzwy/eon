package com.eon.auth.config;/*
 * Copyright 2020-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import com.eon.auth.support.password.PasswordAuthenticationConverter;
import com.eon.auth.support.password.PasswordAuthenticationProvider;
import com.eon.auth.support.user.UserAuthorityService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

/**
 * OAuth2 授权服务器配置类
 *
 * <p>此配置类基于Spring Authorization Server框架，提供完整的OAuth2/OpenID Connect 1.0授权服务器实现。
 * 支持多种标准授权模式，包括授权码模式、客户端凭证模式、设备码模式，以及自定义的密码模式。</p>
 *
 * <p><strong>主要功能特性：</strong></p>
 * <ul>
 *   <li><strong>多授权模式</strong>：authorization_code、client_credentials、device_code、password</li>
 *   <li><strong>OpenID Connect</strong>：完整的OIDC 1.0协议支持，包括ID Token和UserInfo端点</li>
 *   <li><strong>JWT令牌</strong>：基于RSA签名的JWT访问令牌和ID令牌</li>
 *   <li><strong>数据持久化</strong>：使用JDBC存储客户端、授权和同意信息</li>
 *   <li><strong>刷新令牌</strong>：支持长期会话和令牌刷新机制</li>
 *   <li><strong>设备流程</strong>：支持物联网设备的授权流程</li>
 *   <li><strong>mTLS支持</strong>：双向TLS客户端认证</li>
 *   <li><strong>令牌交换</strong>：支持RFC 8693令牌交换协议</li>
 * </ul>
 *
 * <p><strong>安全设计原则：</strong></p>
 * <ul>
 *   <li><strong>最小权限原则</strong>：严格的客户端授权类型和范围控制</li>
 *   <li><strong>令牌安全</strong>：JWT签名、证书绑定、过期策略</li>
 *   <li><strong>客户端认证</strong>：支持多种客户端认证方式</li>
 *   <li><strong>审计追踪</strong>：完整的授权操作记录</li>
 * </ul>
 *
 * <p><strong>数据存储：</strong></p>
 * <ul>
 *   <li>使用H2内存数据库进行演示（生产环境应使用PostgreSQL/MySQL）</li>
 *   <li>oauth2_registered_client：客户端注册信息</li>
 *   <li>oauth2_authorization：授权记录和令牌信息</li>
 *   <li>oauth2_authorization_consent：用户授权同意记录</li>
 * </ul>
 *
 * <p><strong>端点配置：</strong></p>
 * <ul>
 *   <li>/oauth2/authorize：授权端点</li>
 *   <li>/oauth2/token：令牌端点</li>
 *   <li>/oauth2/revoke：令牌撤销端点</li>
 *   <li>/oauth2/introspect：令牌内省端点</li>
 *   <li>/oauth2/device_authorization：设备授权端点</li>
 *   <li>/oauth2/device_verification：设备验证端点</li>
 *   <li>/.well-known/oauth-authorization-server：服务器元数据</li>
 * </ul>
 *
 * @author Joe Grandja
 * @author Daniel Garnier-Moiroux
 * @author Steve Riesenberg
 * @author EON Team
 * @see <a href="https://tools.ietf.org/html/rfc6749">RFC 6749: OAuth 2.0</a>
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core 1.0</a>
 * @since 1.1
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthorizationServerTokenProperties.class)
public class AuthorizationServerConfig {
    /** 自定义同意页面URI */
    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

    /** 授权服务器对外声明的issuer地址，需要与网关等资源服务器保持一致 */
    @Value("${security.oauth2.authorization-server.issuer:http://localhost:3000}")
    private String authorizationServerIssuer;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            OAuth2AuthorizationService authorizationService,
            OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
            AuthenticationManager authenticationManager) throws Exception {

        // 获取OAuth2授权服务器配置器
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = authorizationServer();

        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverters(
                                        authenticationConverters ->// <1>
                                                authenticationConverters.addAll(
                                                        // 自定义授权模式转换器(Converter)
                                                        List.of(
                                                                new PasswordAuthenticationConverter()
                                                        )
                                                )
                                )
                                .authenticationProviders(authenticationProviders ->// <2>
                                        authenticationProviders.addAll(
                                                // 自定义授权模式提供者(Provider)
                                                List.of(
                                                        new PasswordAuthenticationProvider(authorizationService, tokenGenerator, authenticationManager)
                                                )
                                        )
                                )
//                                .accessTokenResponseHandler(new MyAuthenticationSuccessHandler()) // 自定义成功响应
//                                .errorResponseHandler(new MyAuthenticationFailureHandler()) // 自定义失败响应
                );

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
        http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/.well-known/**", "/oauth2/jwks", "/oauth2/token").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .apply(authorizationServerConfigurer);

        // @formatter:on
        return http.build();
    }

    /**
     * 配置JDBC注册客户端仓库
     *
     * <p>此Bean管理OAuth2客户端的注册信息，包括客户端ID、密钥、授权类型、
     * 重定向URI、作用域等。客户端信息存储在数据库中，支持动态管理。</p>
     *
     * <p><strong>预配置的演示客户端：</strong></p>
     * <ul>
     *   <li><strong>messaging-client</strong>：支持多种授权类型的全功能客户端</li>
     *   <li><strong>device-messaging-client</strong>：专用于设备码流程的客户端</li>
     *   <li><strong>token-client</strong>：支持令牌交换的特殊客户端</li>
     *   <li><strong>mtls-demo-client</strong>：演示双向TLS认证的客户端</li>
     * </ul>
     *
     * <p><strong>安全注意事项：</strong></p>
     * <ul>
     *   <li>生产环境应使用强密码并定期轮换客户端密钥</li>
     *   <li>重定向URI应严格限制为可信域名</li>
     *   <li>根据客户端类型合理配置授权类型</li>
     *   <li>谨慎配置客户端作用域，遵循最小权限原则</li>
     * </ul>
     *
     * @param jdbcTemplate    JDBC模板，用于数据库操作
     * @param passwordEncoder 密码编码器，用于编码客户端密钥
     * @return 配置完成的JDBC客户端仓库
     */
    // @formatter:off
    @Bean
    public JdbcRegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        // 使用JDBC客户端仓库，从MySQL数据库读取客户端配置
        // 不再在代码中硬编码客户端信息，而是通过SQL脚本进行数据初始化
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        // 初始化 OAuth2 客户端

        initEONClient(registeredClientRepository,passwordEncoder);
        return registeredClientRepository;
    }
    // @formatter:on

    @Bean
    public JdbcOAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                               RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JdbcOAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                             RegisteredClientRepository registeredClientRepository) {
        // Will be used by the ConsentController
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 配置OAuth2令牌生成器
     *
     * <p>此Bean负责生成各种类型的OAuth2令牌，包括：</p>
     * <ul>
     *   <li><strong>JWT访问令牌</strong>：基于RSA签名的JWT格式访问令牌</li>
     *   <li><strong>不透明访问令牌</strong>：引用令牌格式的访问令牌</li>
     *   <li><strong>刷新令牌</strong>：用于令牌刷新的长期有效令牌</li>
     * </ul>
     *
     * <p><strong>令牌生成器组合：</strong></p>
     * <ol>
     *   <li>JwtGenerator：生成JWT格式的访问令牌和ID令牌</li>
     *   <li>OAuth2AccessTokenGenerator：生成不透明格式的访问令牌</li>
     *   <li>OAuth2RefreshTokenGenerator：生成刷新令牌</li>
     * </ol>
     *
     * <p>使用DelegatingOAuth2TokenGenerator将多个生成器组合，
     * 根据令牌类型和客户端配置选择合适的生成器。</p>
     *
     * @param jwkSource JWK密钥源，用于JWT令牌签名
     * @return 配置完成的令牌生成器
     */
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource,
                                                                      OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        // 1. 创建JWT令牌生成器，用于生成JWT格式的访问令牌
        JwtGenerator jwtGenerator = new JwtGenerator(new NimbusJwtEncoder(jwkSource));
        // 设置JWT令牌自定义器，用于添加自定义claims
        jwtGenerator.setJwtCustomizer(jwtCustomizer);

        // 2. 创建不透明访问令牌生成器，用于生成引用令牌
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();

        // 3. 创建刷新令牌生成器
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        // 4. 组合多个令牌生成器，按优先级顺序排列
        // JwtGenerator优先，如果无法生成JWT则使用不透明令牌
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator,                // JWT访问令牌和ID令牌
                accessTokenGenerator,        // 不透明访问令牌
                refreshTokenGenerator        // 刷新令牌
        );
    }

    /**
     * 配置JWT令牌自定义器
     *
     * <p>用于在JWT令牌中添加自定义的claims信息，如用户角色、权限等。
     * 这个自定义器会在令牌生成时被调用，允许向JWT添加额外的声明。</p>
     *
     * @return JWT令牌自定义器
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer(UserAuthorityService userAuthorityService,
                                                                       AuthorizationServerTokenProperties tokenProperties) {
        return new FederatedIdentityIdTokenCustomizer(
                userAuthorityService,
                tokenProperties.getAccessToken().getAudience()
        );
    }

    /**
     * 配置JWK (JSON Web Key) 密钥源
     *
     * <p>JWK密钥源用于JWT令牌的签名和验证，是OAuth2授权服务器的核心安全组件。</p>
     *
     * <p><strong>功能说明：</strong></p>
     * <ul>
     *   <li><strong>令牌签名</strong>：使用RSA私钥对JWT访问令牌和ID令牌进行签名</li>
     *   <li><strong>令牌验证</strong>：提供RSA公钥供资源服务器验证令牌</li>
     *   <li><strong>密钥轮换</strong>：支持密钥ID标识，便于密钥轮换和管理</li>
     *   <li><strong>JWK端点</strong>：自动暴露 /oauth2/jwks 端点</li>
     * </ul>
     *
     * <p><strong>安全考虑：</strong></p>
     * <ul>
     *   <li>生产环境应使用外部密钥管理服务（如AWS KMS、Azure Key Vault）</li>
     *   <li>私钥应安全存储，不应出现在代码或配置文件中</li>
     *   <li>建议定期轮换密钥以提高安全性</li>
     *   <li>使用足够长度的RSA密钥（推荐2048位或以上）</li>
     * </ul>
     *
     * <p><strong>生产环境改进：</strong></p>
     * <pre>{@code
     * // 从外部密钥管理服务加载
     * RSAPrivateKey privateKey = keyManagementService.getPrivateKey("oauth2-signing-key");
     * RSAPublicKey publicKey = keyManagementService.getPublicKey("oauth2-signing-key");
     * }</pre>
     *
     * @return JWK密钥源，包含RSA密钥对
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // 生成RSA密钥对（演示用途，生产环境应从安全的密钥管理服务加载）
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // kid 使用随机值，保证每次启动都能区分不同的密钥
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("生成 RSA 密钥失败", ex);
        }
    }
    /**
     * 配置JWT解码器
     *
     * <p>JWT解码器用于解析和验证接收到的JWT令牌，主要用于：</p>
     * <ul>
     *   <li><strong>令牌验证</strong>：验证JWT签名的有效性</li>
     *   <li><strong>声明提取</strong>：从JWT中提取用户和客户端信息</li>
     *   <li><strong>过期检查</strong>：验证令牌是否已过期</li>
     *   <li><strong>发行者验证</strong>：验证令牌的发行者是否合法</li>
     * </ul>
     *
     * <p>此Bean由Spring Authorization Server自动配置，
     * 使用相同的JWK源进行令牌验证，确保签名和验证的一致性。</p>
     *
     * @param jwkSource JWK密钥源，用于令牌验证
     * @return 配置完成的JWT解码器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(authorizationServerIssuer)
                .build();
    }


    /**
     * 初始化EON客户端
     *
     * @param registeredClientRepository
     * @param passwordEncoder
     */
    private void initEONClient(JdbcRegisteredClientRepository registeredClientRepository, PasswordEncoder passwordEncoder) {

        String clientId = "eon";
        String clientSecret = "eon";
        String clientName = "eon客户端";

        /*
          如果使用明文，客户端认证时会自动升级加密方式，换句话说直接修改客户端密码，所以直接使用 bcrypt 加密避免不必要的麻烦
          官方ISSUE： https://github.com/spring-projects/spring-authorization-server/issues/1099
         */
        String encodeSecret = passwordEncoder.encode(clientSecret);

        RegisteredClient registeredMallAdminClient = registeredClientRepository.findByClientId(clientId);
        String id = registeredMallAdminClient != null ? registeredMallAdminClient.getId() : UUID.randomUUID().toString();

        RegisteredClient mallAppClient = RegisteredClient.withId(id)
                .clientId(clientId)
                .clientSecret(encodeSecret)
                .clientName(clientName)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD) // 密码模式
                .redirectUri("http://127.0.0.1:8080/authorized")
                .postLogoutRedirectUri("http://127.0.0.1:8080/logged-out")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofDays(1)).build())
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();
        registeredClientRepository.save(mallAppClient);
    }

}
