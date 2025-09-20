package com.eon.user.policy;

import java.util.*;
import java.util.regex.Pattern;

public class CompiledPolicy {
    private Long userId;
    private Integer policyVersion;
    // 菜单权限：permission_key -> ALLOW/DENY
    private Map<String, String> menuEffects;
    // API 规则
    private List<ApiRule> apiRules;

    public static class ApiRule {
        private String key;              // api:GET:/users/:id
        private String method;           // GET/* ...
        private Pattern regex;           // 已编译
        private String effect;           // ALLOW/DENY

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public Pattern getRegex() { return regex; }
        public void setRegex(Pattern regex) { this.regex = regex; }

        public String getEffect() { return effect; }
        public void setEffect(String effect) { this.effect = effect; }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(Integer policyVersion) { this.policyVersion = policyVersion; }

    public Map<String, String> getMenuEffects() { return menuEffects; }
    public void setMenuEffects(Map<String, String> menuEffects) { this.menuEffects = menuEffects; }

    public List<ApiRule> getApiRules() { return apiRules; }
    public void setApiRules(List<ApiRule> apiRules) { this.apiRules = apiRules; }
}