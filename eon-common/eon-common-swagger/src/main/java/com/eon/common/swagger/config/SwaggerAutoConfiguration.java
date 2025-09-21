package com.eon.common.swagger.config;

import com.eon.common.swagger.properties.SwaggerProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * 自动注册基础 OpenAPI 元数据与分组配置。
 */
@AutoConfiguration
@ConditionalOnClass({OpenAPI.class, GroupedOpenApi.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "eon.swagger", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SwaggerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI eonOpenAPI(SwaggerProperties properties) {
        Info info = new Info()
                .title(properties.getTitle())
                .description(properties.getDescription())
                .version(properties.getVersion());
        SwaggerProperties.Contact contact = properties.getContact();
        if (contact != null) {
            info.contact(new Contact().name(contact.getName()).email(contact.getEmail()).url(contact.getUrl()));
        }
        return new OpenAPI().info(info);
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi eonGroupedOpenApi(SwaggerProperties properties) {
        SwaggerProperties.Group firstGroup = properties.getGroups().stream().findFirst().orElse(null);
        GroupedOpenApi.Builder builder;
        if (firstGroup != null && StringUtils.hasText(firstGroup.getName())) {
            builder = GroupedOpenApi.builder()
                    .group(firstGroup.getName())
                    .packagesToScan(StringUtils.commaDelimitedListToStringArray(firstGroup.getPackages()));
        } else {
            builder = GroupedOpenApi.builder()
                    .group("default")
                    .packagesToScan(properties.getBasePackage());
        }
        return builder.build();
    }
}
