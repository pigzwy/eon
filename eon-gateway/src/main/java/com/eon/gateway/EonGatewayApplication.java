package com.eon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EON 网关服务启动类
 * 
 * 基于Spring Cloud Gateway的API网关服务，提供路由转发、负载均衡、安全认证等功能。
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@EnableDiscoveryClient  // 启用Nacos服务发现
@SpringBootApplication
public class EonGatewayApplication {
    
    /**
     * 启动入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonGatewayApplication.class, args);
    }
}
