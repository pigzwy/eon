package com.eon.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EON 授权服务启动类
 * 
 * 基于Spring Security OAuth2 Authorization Server的认证授权服务，提供OAuth2.0标准协议支持。
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@SpringBootApplication
public class EonAuthApplication {
    
    /**
     * 启动入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonAuthApplication.class, args);
    }
}
