package com.eon.common.datasource.config;

import com.eon.common.datasource.properties.DynamicDataSourceProperties;
import com.eon.common.datasource.properties.DynamicDataSourceProperties.TargetDataSource;
import com.eon.common.datasource.support.DataSourceContextHolder;
import com.eon.common.datasource.support.DynamicDataSourceAspect;
import com.eon.common.datasource.support.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 将配置文件声明的数据源统一注册，并提供注解驱动的动态切换能力。
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@ConditionalOnProperty(prefix = "eon.datasource", name = "enabled", havingValue = "true")
public class DynamicDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceContextHolder dataSourceContextHolder() {
        return new DataSourceContextHolder();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    public DataSource dynamicDataSource(DynamicDataSourceProperties properties, DataSourceContextHolder contextHolder) {
        Map<String, DataSource> dataSourceMap = buildDataSources(properties);
        return new DynamicRoutingDataSource(properties.getPrimary(), dataSourceMap, contextHolder);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceAspect dynamicDataSourceAspect(DataSourceContextHolder contextHolder, DynamicDataSourceProperties properties) {
        return new DynamicDataSourceAspect(contextHolder, properties.isStrict(), properties.getTargets().keySet());
    }

    private Map<String, DataSource> buildDataSources(DynamicDataSourceProperties properties) {
        if (CollectionUtils.isEmpty(properties.getTargets())) {
            throw new IllegalStateException("未检测到 eon.datasource.targets 配置，无法初始化动态数据源");
        }
        Map<String, DataSource> result = new LinkedHashMap<>();
        properties.getTargets().forEach((name, definition) -> result.put(name, createDataSource(name, definition)));
        return result;
    }

    private DataSource createDataSource(String name, TargetDataSource definition) {
        validateDefinition(name, definition);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(definition.getUrl());
        config.setUsername(definition.getUsername());
        config.setPassword(definition.getPassword());
        if (StringUtils.hasText(definition.getDriverClassName())) {
            config.setDriverClassName(definition.getDriverClassName());
        }
        definition.getPool().forEach(config::addDataSourceProperty);
        config.setPoolName("eon-" + name + "-pool");
        return new HikariDataSource(config);
    }

    private void validateDefinition(String name, TargetDataSource definition) {
        Objects.requireNonNull(definition, "数据源配置不能为空");
        if (!StringUtils.hasText(definition.getUrl())) {
            throw new IllegalStateException("数据源 " + name + " 缺少 url 配置");
        }
        if (!StringUtils.hasText(definition.getUsername())) {
            throw new IllegalStateException("数据源 " + name + " 缺少 username 配置");
        }
    }
}
