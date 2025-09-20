[Root Directory](../../CLAUDE.md) > **eon-upms**

# EON 用户权限管理模块

## 变更日志 (Changelog)

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立用户权限管理系统文档
- **核心功能**: 提供RBAC权限控制和用户管理
- **模块拆分**: 实现API层和业务层分离

## 模块职责

eon-upms是EON框架的用户权限管理模块，基于RBAC（Role-Based Access Control）模型实现，提供完整的用户管理、角色管理、权限管理功能。该模块采用API层和业务层分离的设计，确保代码结构清晰和复用性。

## 模块结构

```
eon-upms/
├── eon-upms-api/          # API接口层
│   ├── src/main/java/com/eon/upms/api/
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   └── service/       # 服务接口
│   └── pom.xml
└── eon-upms-biz/          # 业务实现层
    ├── src/main/java/com/eon/upms/biz/
    │   ├── controller/    # 控制器
    │   ├── service/impl/  # 服务实现
    │   ├── repository/    # 数据访问
    │   └── config/        # 配置类
    ├── src/main/resources/
    │   └── application.yml
    └── pom.xml
```

## 入口和启动

### 主启动类
```java
// EonUpmsBizApplication.java
@SpringBootApplication
public class EonUpmsBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(EonUpmsBizApplication.class, args);
    }
}
```

### 启动方式
```bash
# 开发模式启动
java -jar eon-upms/eon-upms-biz/target/*.jar

# 生产模式启动
java -jar -Dspring.profiles.active=prod eon-upms/eon-upms-biz/target/*.jar

# Docker方式启动
docker run -p 4000:4000 eon-upms-biz:latest
```

## 外部接口

### 用户管理接口
- **用户列表**: `GET /api/users` - 获取用户列表
- **用户详情**: `GET /api/users/{id}` - 获取用户详情
- **创建用户**: `POST /api/users` - 创建新用户
- **更新用户**: `PUT /api/users/{id}` - 更新用户信息
- **删除用户**: `DELETE /api/users/{id}` - 删除用户

### 角色管理接口
- **角色列表**: `GET /api/roles` - 获取角色列表
- **角色详情**: `GET /api/roles/{id}` - 获取角色详情
- **创建角色**: `POST /api/roles` - 创建新角色
- **更新角色**: `PUT /api/roles/{id}` - 更新角色信息
- **删除角色**: `DELETE /api/roles/{id}` - 删除角色

### 权限管理接口
- **权限列表**: `GET /api/permissions` - 获取权限列表
- **权限详情**: `GET /api/permissions/{id}` - 获取权限详情
- **创建权限**: `POST /api/permissions` - 创建新权限
- **更新权限**: `PUT /api/permissions/{id}` - 更新权限信息
- **删除权限**: `DELETE /api/permissions/{id}` - 删除权限

### 健康检查接口
- **健康状态**: `/actuator/health` - 服务健康状态

## 关键依赖和配置

### API层依赖 (eon-upms-api/pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 业务层依赖 (eon-upms-biz/pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>com.eon</groupId>
        <artifactId>eon-upms-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

### 核心配置 (application.yml)
```yaml
server:
  port: 4000

spring:
  application:
    name: eon-upms-biz
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/eon_upms?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.eon.upms.api.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    call-setters-on-nulls: true
    jdbc-type-for-null: 'null'
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 数据模型

### 用户实体
```java
// eon-upms-api/src/main/java/com/eon/upms/api/entity/User.java
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
```

### 用户DTO
```java
// eon-upms-api/src/main/java/com/eon/upms/api/dto/UserDTO.java
public class UserDTO {
    private Long id;
    private String username;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

### 角色实体
```java
public class Role {
    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
```

### 权限实体
```java
public class Permission {
    private Long id;
    private String permissionName;
    private String permissionCode;
    private String resourceType;
    private String resourceUrl;
    private String method;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer deleted;
}
```

### 用户角色关联实体
```java
public class UserRole {
    private Long id;
    private Long userId;
    private Long roleId;
    private Date createTime;
}
```

### 角色权限关联实体
```java
public class RolePermission {
    private Long id;
    private Long roleId;
    private Long permissionId;
    private Date createTime;
}
```

## 测试和质量

### 健康检查
```java
// HealthController.java
@RestController
public class HealthController {
    @GetMapping("/actuator/health")
    public Map<String, Object> health() {
        return Map.of(
            "service", "eon-upms-biz",
            "status", "UP",
            "time", Instant.now().toString()
        );
    }
}
```

### 接口测试
```bash
# 检查服务健康状态
curl http://localhost:4000/actuator/health

# 通过网关访问服务
curl http://localhost:9999/eon-upms-biz/actuator/health

# 用户管理接口测试
curl -X GET "http://localhost:4000/api/users"
curl -X POST "http://localhost:4000/api/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","email":"test@example.com"}'

# 角色管理接口测试
curl -X GET "http://localhost:4000/api/roles"
curl -X POST "http://localhost:4000/api/roles" \
  -H "Content-Type: application/json" \
  -d '{"roleName":"管理员","roleCode":"admin","description":"系统管理员"}'

# 权限管理接口测试
curl -X GET "http://localhost:4000/api/permissions"
curl -X POST "http://localhost:4000/api/permissions" \
  -H "Content-Type: application/json" \
  -d '{"permissionName":"用户查看","permissionCode":"user:view","resourceType":"menu","resourceUrl":"/api/users","method":"GET"}'
```

### 数据库操作
```sql
-- 创建数据库
CREATE DATABASE eon_upms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户表
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

## 常见问题

### Q: 如何实现RBAC权限控制？
A: 通过用户-角色-权限的三级关联模型，在权限验证时检查用户所拥有的角色和权限。

### Q: 如何支持多租户？
A: 在用户、角色、权限表中添加租户ID字段，在数据查询时过滤租户数据。

### Q: 如何集成认证服务？
A: 通过Feign客户端调用eon-auth服务进行用户认证，获取用户信息和权限。

### Q: 如何实现数据权限？
A: 在业务逻辑中根据用户角色和数据权限规则，动态过滤可访问的数据范围。

## 相关文件列表

### API层文件
- `eon-upms-api/pom.xml` - API层Maven配置
- `eon-upms-api/src/main/java/com/eon/upms/api/dto/UserDTO.java` - 用户DTO
- `eon-upms-api/src/main/java/com/eon/upms/api/entity/User.java` - 用户实体
- `eon-upms-api/src/main/java/com/eon/upms/api/service/UserService.java` - 用户服务接口

### 业务层文件
- `eon-upms-biz/pom.xml` - 业务层Maven配置
- `eon-upms-biz/src/main/java/com/eon/upms/biz/EonUpmsBizApplication.java` - 主启动类
- `eon-upms-biz/src/main/java/com/eon/upms/biz/api/HealthController.java` - 健康检查控制器
- `eon-upms-biz/src/main/resources/application.yml` - 应用配置

### 数据访问层
- `eon-upms-biz/src/main/java/com/eon/upms/biz/repository/UserMapper.java` - 用户数据访问
- `eon-upms-biz/src/main/java/com/eon/upms/biz/repository/RoleMapper.java` - 角色数据访问
- `eon-upms-biz/src/main/java/com/eon/upms/biz/repository/PermissionMapper.java` - 权限数据访问
- `eon-upms-biz/src/main/resources/mapper/UserMapper.xml` - 用户SQL映射

### 服务层
- `eon-upms-biz/src/main/java/com/eon/upms/biz/service/UserServiceImpl.java` - 用户服务实现
- `eon-upms-biz/src/main/java/com/eon/upms/biz/service/RoleServiceImpl.java` - 角色服务实现
- `eon-upms-biz/src/main/java/com/eon/upms/biz/service/PermissionServiceImpl.java` - 权限服务实现

### 控制器层
- `eon-upms-biz/src/main/java/com/eon/upms/biz/controller/UserController.java` - 用户控制器
- `eon-upms-biz/src/main/java/com/eon/upms/biz/controller/RoleController.java` - 角色控制器
- `eon-upms-biz/src/main/java/com/eon/upms/biz/controller/PermissionController.java` - 权限控制器

## 扩展指南

### 添加新的业务功能
1. 在API层定义DTO和实体类
2. 在API层定义服务接口
3. 在业务层实现服务逻辑
4. 在业务层添加控制器
5. 配置数据访问层

### 集成第三方服务
1. 添加Feign客户端配置
2. 定义服务接口
3. 实现服务调用逻辑
4. 处理异常和降级

### 性能优化
1. 配置缓存策略
2. 优化数据库查询
3. 实现分页查询
4. 添加索引优化

### 安全增强
1. 实现数据权限控制
2. 添加操作日志记录
3. 实现敏感数据加密
4. 配置访问频率限制

---

**更新时间**: 2025-09-16 10:40:53  
**文档版本**: v1.0.0  
**维护状态**: 持续更新中