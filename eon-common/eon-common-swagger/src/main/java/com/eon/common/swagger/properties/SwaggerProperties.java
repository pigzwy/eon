package com.eon.common.swagger.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI/Swagger 基础信息配置。
 */
@ConfigurationProperties(prefix = "eon.swagger")
public class SwaggerProperties {

    private boolean enabled = true;
    private String title = "EON 服务 API";
    private String description = "EON 微服务示例接口文档";
    private String version = "1.0.0";
    private String basePackage = "com.eon";
    private Contact contact = new Contact();
    private List<Group> groups = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public static class Contact {
        private String name = "EON 团队";
        private String url = "https://example.com";
        private String email = "support@example.com";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class Group {
        private String name;
        private String packages;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackages() {
            return packages;
        }

        public void setPackages(String packages) {
            this.packages = packages;
        }
    }
}
