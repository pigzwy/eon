package com.eon.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 授权服务器令牌相关的可配置属性。
 */
@ConfigurationProperties(prefix = "security.oauth2.authorization-server")
public class AuthorizationServerTokenProperties {

    private final AccessToken accessToken = new AccessToken();

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public static class AccessToken {

        private List<String> audience = new ArrayList<>();

        public List<String> getAudience() {
            return Collections.unmodifiableList(audience);
        }

        public void setAudience(List<String> audience) {
            this.audience = new ArrayList<>();
            if (audience == null || audience.isEmpty()) {
                return;
            }
            if (audience.size() == 1) {
                String single = audience.get(0);
                if (single != null && single.contains(",")) {
                    for (String part : single.split(",")) {
                        addIfPresent(part);
                    }
                    return;
                }
            }
            for (String value : audience) {
                addIfPresent(value);
            }
        }

        private void addIfPresent(String value) {
            if (value == null) {
                return;
            }
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                audience.add(trimmed);
            }
        }
    }
}
