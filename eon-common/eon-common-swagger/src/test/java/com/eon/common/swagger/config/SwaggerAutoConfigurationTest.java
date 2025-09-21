package com.eon.common.swagger.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ServletWebServerFactoryAutoConfiguration.class,
                    DispatcherServletAutoConfiguration.class,
                    JacksonAutoConfiguration.class,
                    SwaggerAutoConfiguration.class))
            .withPropertyValues("eon.swagger.enabled=true");

    @Test
    void shouldExposeOpenApiBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenAPI.class);
            assertThat(context).hasSingleBean(GroupedOpenApi.class);
            GroupedOpenApi groupedOpenApi = context.getBean(GroupedOpenApi.class);
            assertThat(groupedOpenApi.getGroup()).isEqualTo("default");
        });
    }
}
