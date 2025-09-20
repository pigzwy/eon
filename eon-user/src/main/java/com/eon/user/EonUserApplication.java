package com.eon.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * EON 用户服务启动类
 * 
 * <p>提供完整的用户、角色、权限管理功能的微服务模块</p>
 * 
 * <h3>核心功能:</h3>
 * <ul>
 *   <li>用户管理 - 用户CRUD操作、用户信息维护、多租户隔离</li>
 *   <li>角色管理 - 角色定义、角色分配、角色权限绑定</li>
 *   <li>权限管理 - 细粒度权限控制、资源权限、操作权限</li>
 *   <li>菜单管理 - 动态菜单树、菜单权限、前端导航</li>
 *   <li>策略引擎 - 基于表达式的权限策略编译和执行</li>
 *   <li>API资源 - 后端API接口权限控制和管理</li>
 * </ul>
 * 
 * <h3>技术特点:</h3>
 * <ul>
 *   <li>RBAC权限模型 - 基于角色的访问控制，支持用户-角色-权限三级关联</li>
 *   <li>多租户架构 - 通过tenant_id实现数据隔离，支持SaaS模式</li>
 *   <li>策略编译器 - 动态权限策略编译，支持复杂的权限表达式</li>
 *   <li>缓存优化 - 权限信息缓存，提高权限验证性能</li>
 *   <li>审计日志 - 完整的用户操作日志和权限变更记录</li>
 * </ul>
 * 
 * <h3>端口配置:</h3>
 * <p>默认端口: 4000, 可通过 server.port 配置覆盖</p>
 * 
 * <h3>主要接口:</h3>
 * <ul>
 *   <li>GET /api/users - 用户列表查询</li>
 *   <li>POST /api/users - 创建新用户</li>
 *   <li>GET /api/users/me - 获取当前用户信息</li>
 *   <li>GET /api/roles - 角色列表查询</li>
 *   <li>POST /api/roles - 创建角色</li>
 *   <li>POST /api/roles/{id}/permissions - 分配角色权限</li>
 *   <li>GET /api/menus - 获取菜单树</li>
 *   <li>GET /actuator/health - 健康检查</li>
 * </ul>
 * 
 * <h3>数据模型:</h3>
 * <ul>
 *   <li>User - 用户实体，支持多租户</li>
 *   <li>Role - 角色实体，定义用户角色</li>
 *   <li>Permission - 权限实体，定义具体权限</li>
 *   <li>Menu - 菜单实体，构建前端导航</li>
 *   <li>ApiResource - API资源实体，后端接口权限</li>
 *   <li>UserRole - 用户角色关联</li>
 *   <li>RolePermission - 角色权限关联</li>
 * </ul>
 * 
 * <h3>权限策略:</h3>
 * <ul>
 *   <li>基于路径的权限匹配 - /api/users/** 等路径模式</li>
 *   <li>基于操作的权限控制 - READ、WRITE、DELETE等</li>
 *   <li>基于资源的权限限制 - 特定资源的访问控制</li>
 *   <li>基于条件的动态权限 - 支持表达式和上下文</li>
 * </ul>
 * 
 * @author EON Framework Team
 * @version 1.0.0
 * @since 2025-09-17
 */
@EnableDiscoveryClient  // 启用Nacos服务发现
@SpringBootApplication
public class EonUserApplication {
    
    /**
     * 用户服务启动入口
     * 
     * <p>启动用户权限管理服务，初始化RBAC权限体系</p>
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EonUserApplication.class, args);
    }
}
