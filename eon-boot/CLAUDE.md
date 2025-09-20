[Root Directory](../../CLAUDE.md) > **eon-boot**

# EON 单体启动器模块

## 变更日志 (Changelog)

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立单体应用启动器文档
- **核心功能**: 提供快速原型验证和开发能力
- **配置优化**: 集成Nacos配置中心，支持多环境部署

## 模块职责

eon-boot是EON框架的单体应用启动器，提供快速开发能力，支持从单体应用到微服务架构的平滑演进。该模块集成了Spring Boot的核心功能，提供统一的Web入口和API服务。

## 入口和启动

### 主启动类
```java
// EonBootApplication.java
@SpringBootApplication
public class EonBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonBootApplication.class, args);
    }
}
```

### 启动方式
```bash
# 开发模式启动
java -jar eon-boot/target/*.jar

# 生产模式启动
java -jar -Dspring.profiles.active=prod eon-boot/target/*.jar

# Docker方式启动
docker run -p 9999:9999 eon-boot:latest
```

## 外部接口

### Web接口
- **根路径**: `GET /` - 应用状态检查
- **健康检查**: `GET /actuator/health` - 服务健康状态
- **应用信息**: `GET /actuator/info` - 应用基本信息

### 接口示例
```java
@RestController
static class HelloController {
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
            "app", "eon-boot",
            "status", "ok", 
            "time", Instant.now().toString()
        );
    }
}
```

## 关键依赖和配置

### Maven依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 核心配置
```yaml
server:
  port: 9999

spring:
  application:
    name: eon-boot
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
  config:
    import: optional:nacos:eon-boot.yaml?group=DEFAULT_GROUP
```

### 环境配置
- **开发环境**: `spring.profiles.active=dev`
- **测试环境**: `spring.profiles.active=test`
- **生产环境**: `spring.profiles.active=prod`
- **Docker环境**: `spring.profiles.active=docker`

## 数据模型

当前版本主要提供Web服务能力，暂无复杂的数据模型。后续可集成：
- 用户管理模型
- 配置管理模型
- 日志管理模型

## 测试和质量

### 健康检查
```bash
# 检查应用状态
curl http://localhost:9999/

# 检查健康状态
curl http://localhost:9999/actuator/health

# 检查应用信息
curl http://localhost:9999/actuator/info
```

### 测试策略
- **单元测试**: 集成JUnit 5和Mockito
- **集成测试**: 使用Spring Boot Test框架
- **性能测试**: 使用JMeter进行压力测试

## 常见问题

### Q: 如何从单体应用迁移到微服务？
A: eon-boot设计为兼容模式，可以先在单体模式下开发业务，然后逐步将功能模块拆分为独立的微服务。

### Q: 如何配置多环境？
A: 使用Spring Profile机制，通过`spring.profiles.active`指定环境，配置文件按环境分离。

### Q: 如何集成数据库？
A: 在`application.yml`中添加数据源配置，集成MyBatis Plus进行数据访问。

## 相关文件列表

### 核心文件
- `pom.xml` - Maven项目配置
- `src/main/java/com/eon/boot/EonBootApplication.java` - 主启动类
- `src/main/resources/application.yml` - 应用配置

### 配置文件
- `src/main/resources/application-dev.yml` - 开发环境配置
- `src/main/resources/application-prod.yml` - 生产环境配置
- `src/main/resources/application-docker.yml` - Docker环境配置

### 部署文件
- `Dockerfile` - Docker镜像构建配置
- `docker-compose.yml` - 容器编排配置

## 扩展指南

### 添加新的Web接口
1. 在`com.eon.boot`包下创建Controller类
2. 使用`@RestController`注解标记
3. 定义RESTful API接口
4. 添加必要的业务逻辑

### 集成第三方服务
1. 在pom.xml中添加相关依赖
2. 配置服务连接参数
3. 创建服务客户端类
4. 在Controller中调用服务

### 性能优化
1. 配置JVM参数优化内存使用
2. 启用缓存机制提高响应速度
3. 配置线程池管理并发请求
4. 启用压缩减少网络传输

---

**更新时间**: 2025-09-16 10:40:53  
**文档版本**: v1.0.0  
**维护状态**: 持续更新中