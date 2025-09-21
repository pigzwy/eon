package com.eon.common.datasource.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态数据源配置，允许通过配置文件声明多数据源并指定主库。
 */
@ConfigurationProperties(prefix = "eon.datasource")
public class DynamicDataSourceProperties {

    /**
     * 是否启用动态数据源。
     */
    private boolean enabled = false;

    /**
     * 主数据源标识，默认 master。
     */
    private String primary = "master";

    /**
     * 当找不到声明的数据源时是否抛出异常。
     */
    private boolean strict = true;

    /**
     * 数据源定义列表，按配置顺序注册。
     */
    private Map<String, TargetDataSource> targets = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public Map<String, TargetDataSource> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, TargetDataSource> targets) {
        this.targets = targets;
    }

    /**
     * 单个数据源的连接配置。
     */
    public static class TargetDataSource {

        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private Map<String, String> pool = new LinkedHashMap<>();

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Map<String, String> getPool() {
            return pool;
        }

        public void setPool(Map<String, String> pool) {
            this.pool = pool;
        }
    }
}
