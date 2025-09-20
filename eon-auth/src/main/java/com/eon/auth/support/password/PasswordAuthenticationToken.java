package com.eon.auth.support.password;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * OAuth2 Password授权类型的认证令牌
 * 
 * <p>基于RFC 6749第4.3节定义的资源所有者密码凭证授权（Resource Owner Password Credentials Grant）</p>
 * 
 * <p>此类承载从 /oauth2/token 接口解析出的password授权请求参数，包括：</p>
 * <ul>
 *   <li>客户端认证信息（clientPrincipal）- 必须已通过客户端认证</li>
 *   <li>用户名（username）- 资源所有者的用户名</li>
 *   <li>密码（password）- 资源所有者的密码</li>
 *   <li>作用域（scopes）- 请求的授权范围</li>
 *   <li>额外参数（additionalParameters）- 其他自定义参数</li>
 * </ul>
 * 
 * <p><strong>重要安全提示：</strong>此授权类型应仅用于受信任的客户端，因为它涉及
 * 资源所有者将其凭证直接暴露给客户端。推荐仅在以下场景使用：</p>
 * <ul>
 *   <li>客户端是资源所有者高度信任的第一方应用</li>
 *   <li>其他授权类型（如授权码模式）不可用的情况</li>
 *   <li>从传统应用迁移到OAuth2的过渡期</li>
 * </ul>
 * 
 * <p>此对象在OAuth2授权流程中的作用：</p>
 * <ol>
 *   <li>由 {@link PasswordAuthenticationConverter} 创建</li>
 *   <li>传递给 {@link PasswordAuthenticationProvider} 进行认证处理</li>
 *   <li>包含生成访问令牌所需的所有信息</li>
 * </ol>
 * 
 * @author EON Team
 * @since 1.0.0
 * @see PasswordAuthenticationConverter
 * @see PasswordAuthenticationProvider
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 Section 4.3</a>
 */
public class PasswordAuthenticationToken extends AbstractAuthenticationToken {

    /** 已通过认证的客户端主体信息 */
    private final Authentication clientPrincipal;
    
    /** 资源所有者的用户名 */
    private final String username;
    
    /** 资源所有者的密码 */
    private final String password;
    
    /** 请求的授权范围集合 */
    private final Set<String> scopes;
    
    /** 请求中的额外参数 */
    private final Map<String, Object> additionalParameters;

    /**
     * 构造OAuth2 Password认证令牌
     * 
     * @param clientPrincipal 已通过认证的客户端主体，不能为null
     * @param username 资源所有者的用户名，可以为null（由Provider进行校验）
     * @param password 资源所有者的密码，可以为null（由Provider进行校验）
     * @param scopes 请求的授权范围，可以为null或空集合
     * @param additionalParameters 额外的请求参数，可以为null或空Map
     * @throws IllegalArgumentException 如果clientPrincipal为null
     */
    public PasswordAuthenticationToken(Authentication clientPrincipal,
                                       String username,
                                       String password,
                                       Set<String> scopes,
                                       Map<String, Object> additionalParameters) {
        super(null);
        Assert.notNull(clientPrincipal, "clientPrincipal 不能为空");
        this.clientPrincipal = clientPrincipal;
        this.username = username;
        this.password = password;
        this.scopes = scopes != null ? Collections.unmodifiableSet(scopes) : Collections.emptySet();
        this.additionalParameters = additionalParameters != null ?
                Collections.unmodifiableMap(additionalParameters) : Collections.emptyMap();
        // 设置为未认证状态，由Provider负责认证
        setAuthenticated(false);
    }

    /**
     * 获取已通过认证的客户端主体信息
     * <p>此信息用于后续Provider校验当前客户端是否有权限进行password授权</p>
     * 
     * @return 客户端认证信息，永远不为null
     */
    public Authentication getClientPrincipal() {
        return clientPrincipal;
    }

    /**
     * 获取资源所有者的用户名
     * 
     * @return 用户名，可能为null
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取资源所有者的密码
     * <p><strong>安全注意：</strong>此方法返回明文密码，仅用于认证过程，
     * 认证完成后应立即清除相关内存引用</p>
     * 
     * @return 明文密码，可能为null
     */
    public String getPassword() {
        return password;
    }

    /**
     * 获取请求的授权范围集合
     * 
     * @return 不可变的授权范围集合，永远不为null
     */
    public Set<String> getScopes() {
        return scopes;
    }

    /**
     * 获取请求中的额外参数
     * <p>可用于传递自定义参数，如设备信息、地理位置等</p>
     * 
     * @return 不可变的额外参数Map，永远不为null
     */
    public Map<String, Object> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * 获取认证凭证（密码）
     * <p>实现{@link Authentication}接口的方法</p>
     * 
     * @return 密码凭证
     */
    @Override
    public Object getCredentials() {
        return password;
    }

    /**
     * 获取认证主体（用户名）
     * <p>实现{@link Authentication}接口的方法</p>
     * 
     * @return 用户名主体
     */
    @Override
    public Object getPrincipal() {
        return username;
    }
}
