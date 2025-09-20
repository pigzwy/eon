package com.eon.auth.config;/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import com.eon.auth.support.security.JdbcUserDetailsServiceAdapter;
import com.eon.auth.support.user.UserAuthorityService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * @author Joe Grandja
 * @author Steve Riesenberg
 * @since 1.1
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class  DefaultSecurityConfig {

    // @formatter:off
    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 配置URL访问权限
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 允许匿名访问静态资源和登录页面
                                .requestMatchers("/assets/**", "/login").permitAll()
                                // 授权服务器公开端点与认证接口放行
                                .requestMatchers("/.well-known/**", "/oauth2/jwks", "/oauth2/token", "/api/auth/**").permitAll()
                                // 其他所有请求都需要认证
                                .anyRequest().authenticated()
                )
                // 配置表单登录
                .formLogin(formLogin ->
                        formLogin
                                // 指定自定义登录页面路径
                                .loginPage("/login")
                );

        return http.build();
    }
    // @formatter:on

    /**
     * 获取OAuth2联合身份认证成功处理器
     * 
     * <p>此处理器在OAuth2登录成功后执行，负责处理联合身份信息
     * 和用户账号的关联逻辑。</p>
     * 
     * @return OAuth2认证成功处理器
     */
    private AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new FederatedIdentityAuthenticationSuccessHandler();
    }

    // @formatter:off
    @Bean
    public UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate, UserAuthorityService userAuthorityService) {
        // 统一由数据库驱动的 UserDetailsService 提供账号信息
        return new JdbcUserDetailsServiceAdapter(jdbcTemplate, userAuthorityService);
    }
    // @formatter:on

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 是 Spring Security 默认推荐的密码加密算法
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // 复用 Spring Security 构建好的 AuthenticationManager，供 password Provider 校验用户身份
        return configuration.getAuthenticationManager();
    }

}
