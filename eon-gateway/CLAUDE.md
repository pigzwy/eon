[Root Directory](../../CLAUDE.md) > **eon-gateway**

# EON 网关服务模块

## 变更日志 (Changelog)

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立API网关服务文档
- **核心功能**: 提供统一的API入口和路由转发
- **配置完善**: 集成Spring Cloud Gateway，支持服务发现和负载均衡

## 模块职责

eon-gateway是EON框架的API网关模块，基于Spring Cloud Gateway实现，提供统一的API入口、路由转发、负载均衡、限流熔断等功能。该模块作为微服务架构的核心组件，负责所有外部请求的统一处理和分发。

## 入口和启动

### 主启动类
```java
// EonGatewayApplication.java
@SpringBootApplication
public class EonGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonGatewayApplication.class, args);
    }
}
```

### 启动方式
```bash
# 开发模式启动
java -jar eon-gateway/target/*.jar

# 生产模式启动
java -jar -Dspring.profiles.active=prod eon-gateway/target/*.jar

# Docker方式启动
docker run -p 9999:9999 eon-gateway:latest
```

## 外部接口

### 网关接口
- **服务路由**: `/{service-name}/**` - 路由到对应的服务
- **健康检查**: `/actuator/health` - 网关健康状态
- **网关信息**: `/actuator/info` - 网关基本信息
- **网关路由**: `/actuator/gateway/routes` - 查看所有路由

### 路由示例
- **UPMS服务**: `http://localhost:9999/eon-upms-biz/**` -> `http://localhost:4000/**`
- **Auth服务**: `http://localhost:9999/eon-auth/**` -> `http://localhost:3000/**`
- **Monitor服务**: `http://localhost:9999/eon-monitor/**` -> `http://localhost:5001/**`

## 关键依赖和配置

### Maven依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### 核心配置
```yaml
server:
  port: 9999

spring:
  application:
    name: eon-gateway
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: eon-upms-biz
          uri: lb://eon-upms-biz
          predicates:
            - Path=/eon-upms-biz/**
          filters:
            - StripPrefix=1
        - id: eon-auth
          uri: lb://eon-auth
          predicates:
            - Path=/eon-auth/**
          filters:
            - StripPrefix=1
        - id: eon-monitor
          uri: lb://eon-monitor
          predicates:
            - Path=/eon-monitor/**
          filters:
            - StripPrefix=1

  config:
    import: optional:nacos:eon-gateway.yaml?group=DEFAULT_GROUP

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
```

### 网关过滤器配置
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: Retry
          args:
            retries: 3
            statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 10
            redis-rate-limiter.burstCapacity: 20
```

## 数据模型

### 路由模型
```java
public class RouteDefinition {
    private String id;
    private String uri;
    private List<PredicateDefinition> predicates;
    private List<FilterDefinition> filters;
    private int order;
}
```

### 断言模型
```java
public class PredicateDefinition {
    private String name;
    private Map<String, String> args;
}
```

### 过滤器模型
```java
public class FilterDefinition {
    private String name;
    private Map<String, String> args;
}
```

## 测试和质量

### 路由测试
```bash
# 测试UPMS服务路由
curl http://localhost:9999/eon-upms-biz/actuator/health

# 测试Auth服务路由
curl http://localhost:9999/eon-auth/actuator/health

# 测试监控服务路由
curl http://localhost:9999/eon-monitor/actuator/health
```

### 网关管理
```bash
# 查看所有路由
curl http://localhost:9999/actuator/gateway/routes

# 查看特定路由
curl http://localhost:9999/actuator/gateway/routes/eon-upms-biz

# 刷新路由
curl -X POST http://localhost:9999/actuator/gateway/refresh
```

### 健康检查
```bash
# 检查网关状态
curl http://localhost:9999/actuator/health

# 检查网关信息
curl http://localhost:9999/actuator/info
```

## 常见问题

### Q: 如何配置新的路由规则？
A: 在`spring.cloud.gateway.routes`下添加新的路由配置，包括id、uri、predicates和filters。

### Q: 如何实现限流熔断？
A: 集成Resilience4j或Sentinel，配置断路器和限流规则，在路由过滤器中应用。

### Q: 如何添加跨域支持？
A: 配置CORS过滤器或使用`@CrossOrigin`注解，在网关层面统一处理跨域请求。

### Q: 如何实现请求日志记录？
A: 添加全局过滤器，记录请求和响应信息，包括请求时间、响应状态等。

## 相关文件列表

### 核心文件
- `pom.xml` - Maven项目配置
- `src/main/java/com/eon/gateway/EonGatewayApplication.java` - 主启动类
- `src/main/resources/application.yml` - 应用配置

### 过滤器
- `src/main/java/com/eon/gateway/filter/AuthFilter.java` - 认证过滤器
- `src/main/java/com/eon/gateway/filter/LogFilter.java` - 日志过滤器
- `src/main/java/com/eon/gateway/filter/RateLimitFilter.java` - 限流过滤器

### 配置类
- `src/main/java/com/eon/gateway/config/GatewayConfig.java` - 网关配置
- `src/main/java/com/eon/gateway/config/CorsConfig.java` - 跨域配置
- `src/main/java/com/eon/gateway/config/LoadBalanceConfig.java` - 负载均衡配置

### 工具类
- `src/main/java/com/eon/gateway/util/RouteUtils.java` - 路由工具类
- `src/main/java/com/eon/gateway/util/FilterUtils.java` - 过滤器工具类

## 扩展指南

### 添加自定义过滤器
1. 实现`GlobalFilter`接口
2. 实现`Ordered`接口指定过滤顺序
3. 在`filter`方法中实现过滤逻辑
4. 注册为Spring Bean

### 集成限流熔断
1. 添加Resilience4j或Sentinel依赖
2. 配置断路器和限流规则
3. 在路由过滤器中应用限流逻辑
4. 配置熔断降级策略

### 增强安全性
1. 集成OAuth2认证
2. 添加IP白名单/黑名单
3. 配置请求参数验证
4. 实现防重放攻击

### 性能优化
1. 配置连接池优化性能
2. 启用HTTP/2支持
3. 配置缓存减少后端压力
4. 优化路由匹配性能

### 监控和日志
1. 集成Prometheus监控
2. 配置分布式链路追踪
3. 添加请求日志记录
4. 实现异常告警机制

---

**更新时间**: 2025-09-16 10:40:53  
**文档版本**: v1.0.0  
**维护状态**: 持续更新中