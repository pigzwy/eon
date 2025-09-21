# [Root Directory](../../CLAUDE.md) > **eon-user**

## EON 用户服务模块

### 模块职责

EON用户服务模块提供完整的用户、角色、权限管理功能，是实现企业级RBAC权限体系的核心业务模块。该模块支持多租户架构，提供细粒度的权限控制和动态权限策略管理。

### 入口和启动

**启动类**: `EonUserApplication`
- **包路径**: `com.eon.user.EonUserApplication`
- **端口配置**: 3001 (开发环境) / 4000 (生产环境)
- **启动命令**: `java -jar eon-user/target/*.jar`

**核心注解**:
- `@SpringBootApplication`: Spring Boot应用主类
- `@EnableDiscoveryClient`: 启用Nacos服务发现
- `@EnableFeignClients`: 启用Feign客户端调用

### 外部接口

**REST API接口**:
```yaml
用户管理:
  - GET /users/me - 获取当前用户信息
  - GET /users/{id} - 获取用户详情
  - POST /users - 创建用户
  - PATCH /users/{id} - 更新用户

角色管理:
  - GET /roles - 角色列表查询
  - POST /roles - 创建角色
  - POST /roles/{id}/permissions - 分配角色权限

菜单管理:
  - GET /menus - 获取菜单树

健康检查:
  - GET /actuator/health - 健康状态检查
```

**Feign客户端**:
- `DemoEchoClient`: 示例Feign客户端，用于服务间调用测试

### 关键依赖和配置

**Maven依赖**:
```xml
<!-- Spring Boot Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 数据访问 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 服务发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>

<!-- 配置中心 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

<!-- Feign调用 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**配置文件** (`application.yml`):
```yaml
server:
  port: 3001

spring:
  application:
    name: eon-user
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_HOST:127.0.0.1}:${NACOS_PORT:8848}
      config:
        server-addr: ${spring.cloud.nacos.discovery.server-addr}

# 数据源配置
eon:
  datasource:
    enabled: true
    primary: master
    targets:
      master:
        url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:eon}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
        username: ${MYSQL_USERNAME:root}
        password: ${MYSQL_PASSWORD:root}
```

### 数据模型

**核心实体**:
```yaml
User (用户实体):
  - id: 主键
  - tenantId: 租户ID (多租户支持)
  - username: 用户名 (租户内唯一)
  - email: 邮箱
  - passwordHash: 密码哈希
  - isActive: 是否激活
  - policyVersion: 权限策略版本
  - createdAt/updatedAt: 时间戳

Role (角色实体):
  - id: 主键
  - tenantId: 租户ID
  - code: 角色编码
  - name: 角色名称
  - isSystem: 是否系统角色

Permission (权限实体):
  - id: 主键
  - code: 权限编码
  - name: 权限名称
  - type: 权限类型 (MENU, BUTTON, API)

Menu (菜单实体):
  - id: 主键
  - tenantId: 租户ID
  - parentId: 父菜单ID (树形结构)
  - name: 菜单名称
  - path: 路由路径
  - component: 前端组件

ApiResource (API资源实体):
  - id: 主键
  - path: API路径
  - method: HTTP方法
  - permissionId: 关联权限ID

关联关系:
  - UserRole: 用户角色关联表
  - RolePermission: 角色权限关联表
```

### 测试和质量

**测试文件**:
- `UserPermissionsInterceptorTest.java`: 用户权限拦截器测试
- 其他单元测试和集成测试文件

**测试覆盖**:
- 控制器层API测试
- 服务层业务逻辑测试
- 权限拦截器功能测试
- 数据访问层测试

**质量工具**:
- 统一异常处理
- 参数校验 (Jakarta Validation)
- 日志记录 (SLF4J + Logback)
- 代码规范检查

### 郑州相关文件列表

**核心文件**:
```
src/main/java/com/eon/user/
├── EonUserApplication.java                    # 启动类
├── entity/
│   ├── User.java                             # 用户实体
│   ├── Role.java                             # 角色实体
│   ├── Permission.java                       # 权限实体
│   ├── Menu.java                             # 菜单实体
│   └── ApiResource.java                      # API资源实体
├── dto/                                      # 数据传输对象
│   ├── UserResponse.java
│   ├── UserMeResponse.java
│   ├── CreateUserRequest.java
│   └── UpdateUserRequest.java
├── controller/
│   ├── UserController.java                   # 用户控制器
│   └── HealthController.java                 # 健康检查
├── service/
│   ├── UserApplicationService.java           # 用户应用服务
│   ├── PolicyService.java                    # 权限策略服务
│   └── MenuService.java                      # 菜单服务
├── repository/                               # 数据访问层
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
├── policy/                                   # 权限策略相关
│   ├── CompiledPolicy.java                   # 编译后的权限策略
│   └── PathPatternCompiler.java              # 路径模式编译器
└── remote/feign/                             # Feign客户端
    ├── DemoEchoClient.java
    └── dto/EchoPayload.java

src/main/resources/
├── application.yml                           # 应用配置
├── sql/
│   ├── schema.sql                            # 数据库表结构
│   └── data.sql                              # 初始化数据
└── docker-compose.yml                        # Docker部署配置

src/test/java/com/eon/user/
└── web/
    └── UserPermissionsInterceptorTest.java   # 权限拦截器测试
```

### 变更日志 (Changelog)

### v1.0.0 (2025-09-21)
- **模块初始化完成**: 基于EON框架完成用户服务模块初始化
- **RBAC权限体系**: 实现完整的用户-角色-权限三级权限模型
- **多租户支持**: 支持基于tenant_id的多租户数据隔离
- **权限策略引擎**: 实现基于表达式的动态权限策略编译和执行
- **RESTful API**: 提供标准化的用户管理API接口
- **服务发现集成**: 集成Nacos服务注册发现，支持微服务架构

---

**模块路径**: `/home/pig/github/eon-github/eon-user`  
**维护状态**: 持续开发中  
**技术栈**: Spring Boot 3.5.5 + JPA + Nacos + MySQL  
**主要功能**: 用户管理、角色权限、多租户、权限策略引擎