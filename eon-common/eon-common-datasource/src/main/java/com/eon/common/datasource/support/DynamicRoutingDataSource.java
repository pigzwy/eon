package com.eon.common.datasource.support;

import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 基于 {@link AbstractRoutingDataSource} 的简单实现，结合上下文选择数据源。
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(DynamicRoutingDataSource.class);

    private final String primaryKey;
    private final DataSourceContextHolder contextHolder;

    public DynamicRoutingDataSource(String primaryKey, Map<String, DataSource> targetDataSources,
            DataSourceContextHolder contextHolder) {
        if (targetDataSources == null || targetDataSources.isEmpty()) {
            throw new IllegalArgumentException("targetDataSources 不能为空");
        }
        this.primaryKey = Objects.requireNonNull(primaryKey, "primaryKey");
        this.contextHolder = contextHolder;
        super.setTargetDataSources(new java.util.LinkedHashMap<>(targetDataSources));
        DataSource defaultDataSource = targetDataSources.get(primaryKey);
        if (defaultDataSource == null) {
            defaultDataSource = targetDataSources.values().iterator().next();
            log.warn("主数据源 {} 未在 targets 中声明，将落回 {}", primaryKey, targetDataSources.keySet().iterator().next());
        }
        super.setDefaultTargetDataSource(defaultDataSource);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return contextHolder.peek().orElse(primaryKey);
    }
}
