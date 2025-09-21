package com.eon.common.log.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 提供日志链路追踪配置项，便于按需自定义 header 与 MDC 键名。
 */
@ConfigurationProperties(prefix = "eon.logging.trace")
public class TraceIdProperties {

    /**
     * 是否开启链路追踪过滤器。
     */
    private boolean enabled = true;

    /**
     * 请求头中的链路标识名称。
     */
    private String headerName = "X-Trace-Id";

    /**
     * 存入 MDC 的键名，默认与 header 保持一致。
     */
    private String mdcKey = "traceId";

    /**
     * 当请求头缺失时是否生成新的链路标识。
     */
    private boolean generateIfMissing = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getMdcKey() {
        return mdcKey;
    }

    public void setMdcKey(String mdcKey) {
        this.mdcKey = mdcKey;
    }

    public boolean isGenerateIfMissing() {
        return generateIfMissing;
    }

    public void setGenerateIfMissing(boolean generateIfMissing) {
        this.generateIfMissing = generateIfMissing;
    }
}
