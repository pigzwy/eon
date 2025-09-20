package com.eon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EON 网关服务启动类
 * 
 * <p>基于Spring Cloud Gateway的企业级API网关服务</p>
 * 
 * <h3>核心功能:</h3>
 * <ul>
 *   <li>API路由管理 - 统一入口，动态路由到各个微服务</li>
 *   <li>负载均衡 - 基于Nacos服务发现的智能负载均衡</li>
 *   <li>安全认证 - JWT令牌验证和权限过滤</li>
 *   <li>限流熔断 - 保护后端服务，防止系统过载</li>
 *   <li>请求追踪 - 分布式链路追踪和日志记录</li>
 *   <li>CORS处理 - 统一的跨域资源共享配置</li>
 * </ul>
 * 
 * <h3>技术特点:</h3>
 * <ul>
 *   <li>反应式编程 - 基于Spring WebFlux的非阻塞式处理</li>
 *   <li>服务发现 - 与Nacos无缝集成的服务注册发现</li>
 *   <li>配置管理 - 支持动态配置更新，无需重启服务</li>
 *   <li>监控告警 - 完整的健康检查和指标监控</li>
 * </ul>
 * 
 * <h3>端口配置:</h3>
 * <p>默认端口: 3100, 可通过 server.port 配置覆盖</p>
 * 
 * <h3>路由规则:</h3>
 * <ul>
 *   <li>/{service-name}/** - 自动路由到对应的微服务</li>
 *   <li>/eon-auth/** - 路由到认证服务(端口3000)</li>
 *   <li>/eon-upms-biz/** - 路由到用户权限管理服务(端口4000)</li>
 *   <li>/eon-monitor/** - 路由到监控服务(端口5001)</li>
 *   <li>/actuator/** - 网关管理端点</li>
 * </ul>
 * 
 * <h3>过滤器链:</h3>
 * <ul>
 *   <li>TraceFilter - 生成请求链路追踪ID</li>
 *   <li>AuthenticationFilter - JWT令牌验证</li>
 *   <li>AuthorizationFilter - 权限验证</li>
 *   <li>RequestLogFilter - 请求日志记录</li>
 *   <li>ResponseHeaderFilter - 响应头处理</li>
 * </ul>
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@EnableDiscoveryClient  // 启用Nacos服务发现
@SpringBootApplication
public class EonGatewayApplication {
    
    /**
     * 网关服务启动入口
     * 
     * <p>启动Spring Cloud Gateway，初始化路由规则和过滤器链</p>
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonGatewayApplication.class, args);
    }
}
