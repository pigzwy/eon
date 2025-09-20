package com.eon.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "gateway.cors")
public class GatewayCorsProperties {

    private String allowedOrigins = "*";
    private String allowedHeaders = "Authorization,Content-Type,X-Requested-With";
    private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS";

    public List<String> allowedOrigins() {
        return split(allowedOrigins);
    }

    public List<String> allowedHeaders() {
        return split(allowedHeaders);
    }

    public List<String> allowedMethods() {
        return split(allowedMethods);
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(String allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(String allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    private List<String> split(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
