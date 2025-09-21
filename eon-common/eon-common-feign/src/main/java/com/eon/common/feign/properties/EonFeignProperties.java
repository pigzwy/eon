package com.eon.common.feign.properties;

import com.eon.common.security.constant.AuthHeaderConstants;
import feign.Logger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 控制 Feign 统一行为的配置项。
 */
@ConfigurationProperties(prefix = "eon.feign")
public class EonFeignProperties {

    /**
     * 是否启用增强配置，默认开启。
     */
    private boolean enabled = true;

    /**
     * 是否开启请求头透传。
     */
    private boolean headerPropagationEnabled = true;

    /**
     * 需要透传的请求头，默认会覆盖认证与 TraceId 等核心字段。
     */
    private List<String> headers = new ArrayList<>(List.of(
            AuthHeaderConstants.HDR_AUTHORIZATION,
            AuthHeaderConstants.HDR_X_USER_ID,
            AuthHeaderConstants.HDR_X_TENANT_ID,
                                                                                                                                        AuthHeaderConstants.HDR_X_POLICY_VERSION,
            AuthHeaderConstants.HDR_X_ROLES,
            AuthHeaderConstants.HDR_X_USER_ROLES,
            AuthHeaderConstants.HDR_X_PERMISSIONS,
            AuthHeaderConstants.HDR_X_USER_PERMISSIONS,
            AuthHeaderConstants.HDR_X_TRACE_ID
    ));

    /**
     * 静态请求头，在所有请求中附加。
     */
    private Map<String, String> staticHeaders = new LinkedHashMap<>();

    /**
     * 连接超时时间，默认 3 秒。
     */
    private Duration connectTimeout = Duration.ofSeconds(3);

    /**
     * 读取超时时间，默认 5 秒。
     */
    private Duration readTimeout = Duration.ofSeconds(5);

    /**
     * Feign 日志级别，默认 BASIC。
     */
    private Logger.Level logLevel = Logger.Level.BASIC;

    /**
     * 当请求上下文不存在时，是否尝试从 MDC 中读取 TraceId。
     */
    private boolean fallbackMdcTraceId = true;

    /**
     * 从 MDC 读取 TraceId 时使用的键名。
     */
    private String mdcTraceIdKey = "traceId";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHeaderPropagationEnabled() {
        return headerPropagationEnabled;
    }

    public void setHeaderPropagationEnabled(boolean headerPropagationEnabled) {
        this.headerPropagationEnabled = headerPropagationEnabled;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getStaticHeaders() {
        return staticHeaders;
    }

    public void setStaticHeaders(Map<String, String> staticHeaders) {
        this.staticHeaders = staticHeaders;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Logger.Level getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Logger.Level logLevel) {
        this.logLevel = logLevel;
    }

    public boolean isFallbackMdcTraceId() {
        return fallbackMdcTraceId;
    }

    public void setFallbackMdcTraceId(boolean fallbackMdcTraceId) {
        this.fallbackMdcTraceId = fallbackMdcTraceId;
    }

    public String getMdcTraceIdKey() {
        return mdcTraceIdKey;
    }

    public void setMdcTraceIdKey(String mdcTraceIdKey) {
        this.mdcTraceIdKey = mdcTraceIdKey;
    }
}
