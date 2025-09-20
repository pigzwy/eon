package com.eon.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EON 授权服务启动类
 * 
 * <p>基于Spring Security OAuth2 Authorization Server的企业级认证服务</p>
 * 
 * <h3>核心功能:</h3>
 * <ul>
 *   <li>OAuth2.0 授权服务器 - 支持授权码、客户端凭证、刷新令牌等标准流程</li>
 *   <li>JWT令牌管理 - 生成、验证和撤销访问令牌</li>
 *   <li>用户认证服务 - 集成用户账户和角色权限管理</li>
 *   <li>服务注册发现 - 与Nacos集成的微服务架构</li>
 * </ul>
 * 
 * <h3>外部依赖:</h3>
 * <ul>
 *   <li>数据库: MySQL/H2 - 存储用户、角色、OAuth2客户端信息</li>
 *   <li>Redis: 缓存会话和令牌信息(可选)</li>
 *   <li>Nacos: 服务注册发现和配置中心</li>
 * </ul>
 * 
 * <h3>端口配置:</h3>
 * <p>默认端口: 3000, 可通过 server.port 配置覆盖</p>
 * 
 * <h3>关键端点:</h3>
 * <ul>
 *   <li>GET /oauth2/authorize - OAuth2授权端点</li>
 *   <li>POST /oauth2/token - 令牌获取端点</li>
 *   <li>GET /.well-known/oauth-authorization-server - 服务器配置信息</li>
 *   <li>GET /actuator/health - 健康检查端点</li>
 * </ul>
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@SpringBootApplication
public class EonAuthApplication {
    
    /**
     * 应用程序启动入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonAuthApplication.class, args);
    }
}
