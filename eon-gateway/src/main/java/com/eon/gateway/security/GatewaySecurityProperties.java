package com.eon.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全与鉴权相关配置属性。
 */
@Component
@ConfigurationProperties(prefix = "gateway.security")
public class GatewaySecurityProperties {

    /** JWKS 地址（支持 lb://auth-service） */
    private String jwksUri = "http://auth-service/oauth2/jwks";

    /** 允许匿名访问（白名单）路径模式 */
    private List<String> whitelist = new ArrayList<>();

    /** 期望的 issuer（可选） */
    private String issuer;

    /** 期望的 audience（可选） */
    private String audience;

    public GatewaySecurityProperties() {
        // 默认白名单，可在配置中心按需覆盖
        whitelist.add("/.well-known/**");
        whitelist.add("/oauth2/jwks");
        whitelist.add("/oauth2/token");
        whitelist.add("/oauth2/introspect");
        whitelist.add("/api/auth/login");
        whitelist.add("/api/auth/refresh");
        whitelist.add("/actuator/**");
        whitelist.add("/health");
    }

    public String getJwksUri() {
        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
