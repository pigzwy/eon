package com.eon.auth.support.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.util.LinkedMultiValueMap;

/**
 * OAuth2 Password授权类型请求转换器
 * 
 * <p>负责将针对 /oauth2/token 端点的HTTP POST请求转换为 {@link PasswordAuthenticationToken}，
 * 以便后续的 {@link PasswordAuthenticationProvider} 进行处理。</p>
 * 
 * <p><strong>请求格式要求：</strong></p>
 * <ul>
 *   <li>HTTP方法：必须为POST</li>
 *   <li>Content-Type：application/x-www-form-urlencoded</li>
 *   <li>客户端认证：通过HTTP Basic Authentication或client_secret_post</li>
 * </ul>
 * 
 * <p><strong>支持的请求参数：</strong></p>
 * <ul>
 *   <li><code>grant_type</code>：必须为"password"</li>
 *   <li><code>username</code>：资源所有者的用户名，必需</li>
 *   <li><code>password</code>：资源所有者的密码，必需</li>
 *   <li><code>scope</code>：请求的授权范围，可选，多个scope用空格分隔</li>
 *   <li>其他自定义参数：会被保存在additionalParameters中</li>
 * </ul>
 * 
 * <p><strong>示例HTTP请求：</strong></p>
 * <pre>{@code
 * POST /oauth2/token HTTP/1.1
 * Host: authorization-server.example.com
 * Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
 * Content-Type: application/x-www-form-urlencoded
 * 
 * grant_type=password&username=johndoe&password=A3ddj3w&scope=read write
 * }</pre>
 * 
 * <p><strong>转换逻辑：</strong></p>
 * <ol>
 *   <li>验证HTTP方法为POST</li>
 *   <li>检查grant_type参数是否为"password"</li>
 *   <li>从SecurityContext获取已认证的客户端信息</li>
 *   <li>提取username和password参数</li>
 *   <li>解析scope参数（如果存在）</li>
 *   <li>收集其他额外参数</li>
 *   <li>构造并返回OAuth2PasswordAuthenticationToken</li>
 * </ol>
 * 
 * <p><strong>错误处理策略：</strong></p>
 * <ul>
 *   <li>如果不是POST请求或grant_type不是"password"，返回null</li>
 *   <li>如果username或password为空，仍创建Token（由Provider抛出具体错误）</li>
 *   <li>这样设计是为了保持与Spring Security标准错误处理的一致性</li>
 * </ul>
 * 
 * @author EON Team
 * @since 1.0.0
 * @see PasswordAuthenticationToken
 * @see PasswordAuthenticationProvider
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3.2">RFC 6749 Section 4.3.2</a>
 */
public class PasswordAuthenticationConverter implements AuthenticationConverter {

    /** Password授权类型常量 */
    public static final AuthorizationGrantType PASSWORD_GRANT_TYPE = new AuthorizationGrantType("password");

    /**
     * 将HTTP请求转换为OAuth2 Password认证令牌
     * 
     * <p>此方法是转换器的核心逻辑，负责：</p>
     * <ul>
     *   <li>验证请求方法和授权类型</li>
     *   <li>提取和验证请求参数</li>
     *   <li>构造认证令牌对象</li>
     * </ul>
     * 
     * @param request HTTP请求对象，不能为null
     * @return OAuth2PasswordAuthenticationToken 如果请求符合password授权模式，
     *         null 如果请求不是password授权（让其他转换器处理）
     * @throws RuntimeException 如果参数提取过程中发生错误
     */
    @Override
    public Authentication convert(HttpServletRequest request) {
        // 只处理POST请求，其他HTTP方法返回null
        if (!"POST".equals(request.getMethod())) {
            return null;
        }

        // 提取并解析表单参数
        MultiValueMap<String, String> params = extractRequestParameters(request);
        String grantType = params.getFirst("grant_type");
        
        // 只处理password授权类型的请求
        if (!"password".equals(grantType)) {
            return null;
        }

        // 从SecurityContext获取已通过客户端认证的主体信息
        // 注意：此时客户端必须已经通过HTTP Basic或client_secret_post等方式完成认证
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        // 提取必需的用户凭证参数
        String username = params.getFirst("username");
        String password = params.getFirst("password");
        
        // 如果用户名或密码为空，仍然创建Token让Provider处理
        // 这样可以让Provider抛出标准的OAuth2错误响应
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            // 注释：这里不直接返回null，而是让Provider抛出invalid_request异常
            // 这样确保错误处理与OAuth2标准一致
        }

        // 解析scope参数，多个scope用空格分隔
        Set<String> scopes = new HashSet<>();
        String scope = params.getFirst("scope");
        if (StringUtils.hasText(scope)) {
            // 按空格拆分scope字符串，符合RFC 6749规范
            scopes.addAll(Arrays.asList(scope.split("\\s+")));
        }

        // 收集除标准参数外的所有额外参数
        Map<String, Object> additional = new HashMap<>();
        params.forEach((key, value) -> {
            // 排除OAuth2标准参数，其余参数作为附加信息
            if (!List.of("grant_type", "username", "password", "scope").contains(key)) {
                // 如果参数只有一个值，存储字符串；多个值存储列表
                additional.put(key, value.size() == 1 ? value.get(0) : value);
            }
        });

        // 构造并返回Password认证令牌
        return new PasswordAuthenticationToken(clientPrincipal, username, password, scopes, additional);
    }

    /**
     * 从HTTP请求中提取所有表单参数
     * 
     * <p>此方法处理application/x-www-form-urlencoded类型的请求体，
     * 将所有参数解析为MultiValueMap格式以支持多值参数。</p>
     * 
     * @param request HTTP请求对象
     * @return 包含所有请求参数的MultiValueMap，永远不为null
     */
    private MultiValueMap<String, String> extractRequestParameters(HttpServletRequest request) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // 遍历所有请求参数，支持参数多值情况
        request.getParameterMap().forEach((key, values) -> {
            if (values != null) {
                for (String value : values) {
                    params.add(key, value);
                }
            }
        });
        return params;
    }
}
