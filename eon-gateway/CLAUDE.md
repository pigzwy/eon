# [Root Directory](../../CLAUDE.md) > **eon-gateway**

## EON 网关服务模块

### 模块职责

EON网关服务模块是基于Spring Cloud Gateway的企业级API网关服务，提供统一的API入口、路由转发、负载均衡、安全认证、限流熔断等核心网关功能，是微服务架构中的重要基础设施。

### 入口和启动

**启动类**: `EonGatewayApplication`
- **包路径**: `com.eon.gateway.EonGatewayApplication`
- **端口配置**: 3100
- **启动命令**: `java -jar eon-gateway/target/*.jar`

**核心注解**:
- `@SpringBootApplication`: Spring Boot应用主类
- `@EnableDiscoveryClient`: 启用Nacos服务发现

### 外部接口

**网关路由**:
```yaml
用户API路由:
  - Path: /api/users/**
  - 目标服务: eon-user
  - 端口: 4000
  - 过滤器: 认证、授权、限流、熔断、重试

认证服务路由:
  - Path: /.well-known/**, /oauth2/**, /userinfo
  - 目标服务: eon-auth
  - 端口: 3000
  - 过滤器: 无需认证

用户权限路由:
  - Path: /eon-upms-biz/**
  - 目标服务: eon-upms-biz
  - 端口: 3200
  - 过滤器: 认证、授权

监控服务路由:
  - Path: /eon-monitor/**
  - 目标服务: eon-monitor
  - 端口: 5001
  - 过滤器: 认证

管理端点:
  - Path: /actuator/**
  - 目标: 网关自身
  - 功能: 健康检查、指标监控、路由管理
```

### 关键依赖和配置

**Maven依赖**:
```xml
<!-- Spring Cloud Gateway -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- 服务发现与配置中心 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

<!-- 断路器/重试 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>

<!-- Redis限流 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- 负载均衡 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>

<!-- 监控指标 -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**配置文件** (`application.yml`):
```yaml
server:
  port: 3100

spring:
  application:
    name: eon-gateway
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_HOST:127.0.0.1}:${NACOS_PORT:8848}
      config:
        server-addr: ${spring.cloud.nacos.discovery.server-addr}
    gateway:
      discovery:
        locator:
          enabled: false
      default-filters:
        - RemoveRequestHeader=Cookie
        - RemoveRequestHeader=X-User-Id
        - RemoveRequestHeader=X-User-Roles
        - SaveSession
      routes:
        - id: user-api
          uri: lb://eon-user
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: RequestSize
              args: { maxSize: 10MB }
            - name: Retry
              args: { retries: 2, statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE, methods: GET }
            - name: CircuitBreaker
              args: { name: userApiCB, fallbackUri: forward:/__fallback/user }
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20
                redis-rate-limiter.burstCapacity: 40
                key-resolver: "#{@userKeyResolver}"
          metadata:
            requiredRoles: ["USER_READ"]
        
        - id: auth-api
          uri: http://eon-auth:3000
          predicates:
            - Path=/.well-known/**,/oauth2/**,/userinfo
          filters:
            - StripPrefix=0

  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${AUTH_SERVER_ISSUER_URI:http://localhost:3000}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,gateway,env
  endpoint:
    health:
      show-details: when_authorized

gateway:
  security:
    jwksUri: ${AUTH_SERVER_JWKS_URI:http://eon-auth:3000/oauth2/jwks}
    issuer: ${AUTH_SERVER_ISSUER_URI:http://localhost:3000}
    whitelist:
      - /.well-known/**
      - /oauth2/jwks
      - /oauth2/token
      - /oauth2/introspect
      - /api/auth/login
      - /api/auth/refresh
      - /actuator/**
      - /health
  cors:
    allowed-origins: http://localhost:5173
```

### 过滤器链设计

**过滤器执行顺序**:
```yaml
1. TraceFilter (执行顺序: HIGHEST_PRECEDENCE + 1)
   - 功能: 生成请求链路追踪ID
   - 类型: GlobalFilter
   - 位置: com.eon.gateway.filter.TraceFilter

2. JwtAuthenticationFilter (执行顺序: -10)
   - 功能: JWT令牌验证和解析
   - 类型: GlobalFilter
   - 位置: com.eon.gateway.filter.JwtAuthenticationFilter

3. AuthorizationFilter (执行顺序: -5)
   - 功能: 基于角色的权限验证
   - 类型: GlobalFilter
   - 位置: com.eon.gateway.filter.AuthorizationFilter

4. RequestLogFilter (执行顺序: 0)
   - 功能: 请求日志记录和监控
   - 类型: GlobalFilter
   - 位置: com.eon.gateway.filter.RequestLogFilter

5. ResponseHeaderFilter (执行顺序: LOWEST_PRECEDENCE - 1)
   - 功能: 响应头处理和安全加固
   - 类型: GlobalFilter
   - 位置: com.eon.gateway.filter.ResponseHeaderFilter
```

### 安全认证机制

**JWT令牌验证**:
- 验证JWT令牌的签名和有效期
- 解析用户身份信息和权限
- 支持令牌自动刷新机制
- 集成Spring Security OAuth2 Resource Server

**权限控制**:
- 基于路由元数据的角色权限配置
- 支持细粒度的API权限控制
- 集成RBAC权限模型
- 支持白名单路径配置

**安全特性**:
- CORS跨域资源共享配置
- CSRF防护机制
- XSS攻击防护
- 请求大小限制
- 敏感信息过滤

### 限流和熔断

**限流机制**:
- 基于Redis的分布式限流
- 支持用户级别和IP级别限流
- 可配置的令牌桶算法参数
- 支持突发流量处理

**熔断机制**:
- 基于Resilience4j的熔断器
- 支持服务降级和回退
- 可配置的熔断策略
- 熔断状态监控和恢复

**重试机制**:
- 智能重试策略配置
- 支持特定HTTP状态码重试
- 可配置的重试次数和间隔
- 避免重复提交保护

### 监控和运维

**健康检查**:
- 网关服务健康状态
- 后端服务连接状态
- 资源使用情况监控
- 自动故障检测和恢复

**性能监控**:
- 请求响应时间统计
- 吞吐量和并发量监控
- 错误率和异常统计
- 系统资源使用情况

**路由管理**:
- 动态路由配置
- 路由健康检查
- 路由权重分配
- 灰度发布支持

### 测试和质量

**测试文件**:
- `JwtAuthenticationFilterTest.java`: JWT认证过滤器测试

**测试覆盖**:
- 过滤器功能测试
- 路由配置测试
- 安全认证测试
- 限流熔断测试

### 重要文件列表

**核心文件**:
```
src/main/java/com/eon/gateway/
├── EonGatewayApplication.java                # 启动类
├── config/
│   ├── GatewayConfiguration.java             # 网关配置
│   ├── GatewayCorsProperties.java           # CORS配置属性
│   ├── GatewaySecurityProperties.java       # 安全配置属性
│   └── WebClientConfig.java                  # WebClient配置
├── security/
│   ├── AuthContext.java                     # 认证上下文
│   ├── AuthConstants.java                   # 认证常量
│   └── GatewaySecurityProperties.java       # 网关安全属性
├── filter/
│   ├── AuthorizationFilter.java            # 权限过滤器
│   ├── JwtAuthenticationFilter.java        # JWT认证过滤器
│   ├── RequestLogFilter.java                # 请求日志过滤器
│   ├── ResponseHeaderFilter.java           # 响应头过滤器
│   └── TraceFilter.java                    # 链路追踪过滤器
├── support/
│   └── UserKeyResolver.java                 # 用户限流解析器
├── controller/
│   └── FallbackController.java              # 熔断回退控制器
└── util/
    └── PathMatcherUtils.java                # 路径匹配工具

src/main/resources/
└── application.yml                           # 应用配置

src/test/java/com/eon/gateway/
└── filter/
    └── JwtAuthenticationFilterTest.java     # JWT过滤器测试
```

### 变更日志 (Changelog)

### v2.0.0 (2025-09-21)
- **AI上下文深度初始化**: 完成网关模块的深度分析和文档更新
- **安全认证增强**: 完整的JWT令牌验证和基于角色的权限控制体系
- **过滤器链重构**: 完整的5层过滤器链，支持追踪、认证、授权、日志、响应处理
- **限流熔断完善**: Redis分布式限流和Resilience4j熔断机制
- **监控运维增强**: 完整的健康检查、性能监控和路由管理功能
- **端口配置更新**: 网关端口从9999更新为3100，避免端口冲突

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立API网关服务文档
- **核心功能**: 提供统一的API入口和路由转发
- **配置完善**: 集成Spring Cloud Gateway，支持服务发现和负载均衡

---

**模块路径**: `/home/pig/github/eon-github/eon-gateway`  
**维护状态**: 持续开发中  
**技术栈**: Spring Cloud Gateway + Spring Security OAuth2 + Nacos + Redis + Resilience4j  
**主要功能**: API路由、负载均衡、安全认证、限流熔断、监控运维