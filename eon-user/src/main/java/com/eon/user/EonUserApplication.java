package com.eon.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * EON 用户服务启动类
 * 
 * 提供完整的用户、角色、权限管理功能，支持多租户架构和RBAC权限模型。
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@EnableDiscoveryClient  // 启用Nacos服务发现
@EnableFeignClients(basePackages = "com.eon.user.remote.feign")
@SpringBootApplication
public class EonUserApplication {
    
    /**
     * 启动入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonUserApplication.class, args);
    }
}
