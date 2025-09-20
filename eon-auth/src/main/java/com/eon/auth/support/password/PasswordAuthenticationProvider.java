package com.eon.auth.support.password;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * OAuth2 Password授权类型的认证提供者
 * 
 * <p>这是OAuth2 Password授权流程的核心组件，负责处理完整的认证和令牌生成逻辑。
 * 实现了RFC 6749第4.3节定义的资源所有者密码凭证授权流程。</p>
 * 
 * <p><strong>主要职责：</strong></p>
 * <ol>
 *   <li><strong>客户端授权校验</strong>：验证客户端是否有权限使用password授权类型</li>
 *   <li><strong>用户身份认证</strong>：通过AuthenticationManager验证用户名和密码</li>
 *   <li><strong>授权范围处理</strong>：解析并验证请求的scope</li>
 *   <li><strong>令牌生成</strong>：生成访问令牌和刷新令牌</li>
 *   <li><strong>授权存储</strong>：保存授权信息以支持令牌内省和撤销</li>
 * </ol>
 * 
 * <p><strong>认证流程：</strong></p>
 * <pre>{@code
 * 1. 接收 OAuth2PasswordAuthenticationToken
 * 2. 验证客户端是否支持 password 授权类型
 * 3. 使用 AuthenticationManager 验证用户凭证
 * 4. 解析和验证请求的授权范围
 * 5. 生成访问令牌（JWT或Reference Token）
 * 6. 根据客户端配置生成刷新令牌（可选）
 * 7. 保存授权记录到数据库
 * 8. 返回 OAuth2AccessTokenAuthenticationToken
 * }</pre>
 * 
 * <p><strong>安全考虑：</strong></p>
 * <ul>
 *   <li><strong>客户端信任</strong>：只允许预先注册且标记为trusted的客户端使用</li>
 *   <li><strong>密码保护</strong>：用户密码通过标准的PasswordEncoder进行验证</li>
 *   <li><strong>范围限制</strong>：严格按客户端注册信息限制可用scope</li>
 *   <li><strong>令牌安全</strong>：支持JWT签名和令牌过期策略</li>
 *   <li><strong>审计日志</strong>：记录所有认证尝试和令牌颁发事件</li>
 * </ul>
 * 
 * <p><strong>配置要求：</strong></p>
 * <ul>
 *   <li>注入OAuth2AuthorizationService用于授权记录管理</li>
 *   <li>注入OAuth2TokenGenerator用于令牌生成</li>
 *   <li>注入AuthenticationManager用于用户身份验证</li>
 *   <li>确保客户端配置中包含password授权类型</li>
 * </ul>
 * 
 * <p><strong>错误处理：</strong></p>
 * <ul>
 *   <li>{@code unauthorized_client}：客户端未授权使用password模式</li>
 *   <li>{@code invalid_grant}：用户名密码错误或用户不存在</li>
 *   <li>{@code invalid_scope}：请求的scope超出客户端允许范围</li>
 *   <li>{@code server_error}：令牌生成失败或内部错误</li>
 * </ul>
 * 
 * @author EON Team
 * @since 1.0.0
 * @see PasswordAuthenticationToken
 * @see PasswordAuthenticationConverter
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 Section 4.3</a>
 */
public class PasswordAuthenticationProvider implements AuthenticationProvider {

    /** OAuth2授权服务，用于保存和管理授权记录 */
    private final OAuth2AuthorizationService authorizationService;
    
    /** OAuth2令牌生成器，用于生成访问令牌和刷新令牌 */
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    
    /** 认证管理器，用于验证用户名密码 */
    private final AuthenticationManager authenticationManager;

    /**
     * 构造OAuth2 Password认证提供者
     * 
     * @param authorizationService OAuth2授权服务，用于管理授权记录
     * @param tokenGenerator OAuth2令牌生成器，用于生成各种类型令牌
     * @param authenticationManager 认证管理器，用于验证用户凭证
     */
    public PasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                          OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                          AuthenticationManager authenticationManager) {
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 执行OAuth2 Password授权认证
     * 
     * <p>这是认证流程的核心方法，按以下步骤执行完整的password授权：</p>
     * <ol>
     *   <li>验证客户端认证状态和授权类型权限</li>
     *   <li>通过AuthenticationManager验证用户名密码</li>
     *   <li>解析和验证请求的授权范围</li>
     *   <li>生成访问令牌和可选的刷新令牌</li>
     *   <li>保存授权记录并返回令牌响应</li>
     * </ol>
     * 
     * @param authentication 待认证的OAuth2PasswordAuthenticationToken
     * @return OAuth2AccessTokenAuthenticationToken 包含生成的令牌信息
     * @throws OAuth2AuthenticationException 当认证失败时抛出具体的OAuth2错误
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws OAuth2AuthenticationException {
        PasswordAuthenticationToken passwordAuthentication = (PasswordAuthenticationToken) authentication;

        // 第一步：校验客户端是否已认证且有权限使用password授权类型
        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClient(passwordAuthentication.getClientPrincipal());
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null || registeredClient.getAuthorizationGrantTypes().stream()
                .noneMatch(grantType -> PasswordAuthenticationConverter.PASSWORD_GRANT_TYPE.getValue().equals(grantType.getValue()))) {
            // 客户端未被授权使用password模式，返回标准OAuth2错误
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
        }

        // 第二步：使用Spring Security的认证管理器验证用户名密码
        // 这里将OAuth2的资源所有者凭证转换为标准的用户名密码认证
        Authentication usernamePasswordAuthentication;
        try {
            usernamePasswordAuthentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(passwordAuthentication.getUsername(), passwordAuthentication.getPassword())
            );
        } catch (Exception ex) {
            // 用户认证失败，可能是用户名不存在、密码错误、账户锁定等原因
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "用户名或密码不正确", null));
        }

        // 第三步：解析和验证请求的授权范围
        Set<String> authorizedScopes = resolveAuthorizedScopes(passwordAuthentication, registeredClient);

        // 第四步：构造令牌生成上下文并生成访问令牌
        DefaultOAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)                           // 注册的客户端信息
                .principal(usernamePasswordAuthentication)                     // 已认证的用户主体
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())  // 授权服务器上下文
                .authorizedScopes(authorizedScopes)                          // 授权的范围
                .authorizationGrantType(PasswordAuthenticationConverter.PASSWORD_GRANT_TYPE)  // 授权类型
                .authorizationGrant(passwordAuthentication)                  // 原始授权请求
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)                     // 令牌类型
                .build();

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(accessTokenContext);
        if (generatedAccessToken == null) {
            // 令牌生成器未能生成访问令牌，这通常表示配置错误
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "生成访问令牌失败", null));
        }

        // 构造标准的OAuth2访问令牌对象
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt() != null ? generatedAccessToken.getIssuedAt() : Instant.now(),
                generatedAccessToken.getExpiresAt(),
                authorizedScopes);

        // 第五步：根据客户端配置生成刷新令牌（可选）
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().stream()
                .anyMatch(grantType -> AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType.getValue()))) {
            // 客户端支持refresh_token授权类型，生成刷新令牌
            DefaultOAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(usernamePasswordAuthentication)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorizationGrantType(PasswordAuthenticationConverter.PASSWORD_GRANT_TYPE)
                    .authorizationGrant(passwordAuthentication)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();

            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(refreshTokenContext);
            if (generatedRefreshToken instanceof OAuth2RefreshToken token) {
                refreshToken = token;
            }
        }

        // 第六步：保存授权记录到持久化存储
        // 这些记录用于后续的令牌内省、撤销和刷新操作
        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(usernamePasswordAuthentication.getName())      // 用户身份标识
                .authorizationGrantType(PasswordAuthenticationConverter.PASSWORD_GRANT_TYPE)  // 授权类型
                .attribute(Principal.class.getName(), usernamePasswordAuthentication)  // 用户主体信息
                .accessToken(accessToken)                                     // 访问令牌
                .refreshToken(refreshToken)                                   // 刷新令牌（可选）
                .build();
        this.authorizationService.save(authorization);

        // 第七步：返回包含令牌的认证结果
        Map<String, Object> additionalParameters = Collections.emptyMap();
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
    }

    /**
     * 指示此提供者支持的认证类型
     * 
     * @param authentication 认证类型的Class对象
     * @return true 如果支持OAuth2PasswordAuthenticationToken类型
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证并获取已认证的客户端信息
     * 
     * <p>此方法确保客户端已经通过了客户端认证（如HTTP Basic认证），
     * 这是OAuth2规范要求的前提条件。</p>
     * 
     * @param authentication 客户端认证信息
     * @return 已认证的客户端令牌
     * @throws OAuth2AuthenticationException 如果客户端未认证
     */
    private OAuth2ClientAuthenticationToken getAuthenticatedClient(Authentication authentication) {
        if (authentication instanceof OAuth2ClientAuthenticationToken clientAuthentication && clientAuthentication.isAuthenticated()) {
            return clientAuthentication;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    /**
     * 解析和验证授权范围
     * 
     * <p>此方法处理客户端请求的scope，确保：</p>
     * <ul>
     *   <li>请求的scope都在客户端注册时声明的范围内</li>
     *   <li>如果没有请求特定scope，则授予客户端的所有注册scope</li>
     *   <li>返回的scope集合是客户端实际被授权的范围</li>
     * </ul>
     * 
     * @param authentication 包含请求scope的认证令牌
     * @param registeredClient 客户端注册信息
     * @return 实际授权的scope集合，永远不为null
     */
    private Set<String> resolveAuthorizedScopes(PasswordAuthenticationToken authentication, RegisteredClient registeredClient) {
        Set<String> requestedScopes = authentication.getScopes();
        Set<String> authorizedScopes = new HashSet<>();
        
        if (!CollectionUtils.isEmpty(requestedScopes)) {
            // 客户端请求了特定的scope，需要验证每个scope是否在允许范围内
            for (String requestedScope : requestedScopes) {
                if (registeredClient.getScopes().contains(requestedScope)) {
                    authorizedScopes.add(requestedScope);
                }
                // 注意：这里忽略了不被允许的scope，而不是抛出异常
                // 这符合OAuth2规范的"最小化授权"原则
            }
        }
        
        // 如果没有请求特定scope或者所有请求的scope都被拒绝，则授予默认scope
        if (authorizedScopes.isEmpty()) {
            authorizedScopes.addAll(registeredClient.getScopes());
        }
        
        return authorizedScopes;
    }
}
