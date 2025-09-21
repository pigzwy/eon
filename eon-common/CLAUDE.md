# [Root Directory](../../CLAUDE.md) > **eon-common**

## EON 公共组件模块

### 模块职责

eon-common是EON框架的公共组件模块，提供统一的基础设施、工具类、安全组件、数据访问组件等，确保整个系统的一致性和可维护性。该模块采用分层设计，包含12个子模块，各个子模块职责明确，便于维护和扩展。

### 模块结构

```
eon-common/
├── eon-common-bom/          # 依赖管理BOM
├── eon-common-core/         # 核心工具类
├── eon-common-security/     # 安全组件
├── eon-common-mybatis/      # MyBatis扩展
├── eon-common-datasource/   # 动态数据源
├── eon-common-feign/        # Feign扩展
├── eon-common-log/          # 日志组件
├── eon-common-oss/          # 文件上传
├── eon-common-seata/        # 分布式事务
├── eon-common-websocket/    # WebSocket
├── eon-common-swagger/      # 接口文档
└── eon-common-xss/          # XSS防护
```

### 核心组件说明

#### eon-common-bom
**功能**: 统一依赖版本管理，确保各个模块使用相同的依赖版本

**主要特性**:
- 集中管理第三方依赖版本
- 避免版本冲突
- 便于版本升级

#### eon-common-core
**功能**: 核心工具类和基础模型

**主要特性**:
- 统一返回结果封装 (`R<T>`)
- 分页结果封装
- 错误码定义
- 通用异常类
- 日期时间工具类
- 字符串工具类
- 常量定义

**关键文件**:
- `R.java`: 统一响应结果封装
- `ServiceNameConstants.java`: 服务名称常量
- `CommonConstants.java`: 通用常量定义
- `SecurityConstants.java`: 安全相关常量
- `CacheConstants.java`: 缓存相关常量

#### eon-common-security
**功能**: 安全相关组件

**主要特性**:
- 用户上下文管理 (`UserContextHolder`)
- 认证用户信息 (`AuthenticatedUser`)
- 当前用户注解 (`@CurrentUser`)
- 权限上下文拦截器
- 网关认证上下文提取器
- 认证头常量定义

**关键文件**:
- `UserContextHolder.java`: 用户上下文持有者
- `AuthenticatedUser.java`: 认证用户信息
- `CurrentUser.java`: 当前用户注解
- `UserPermissionsInterceptor.java`: 用户权限拦截器
- `GatewayAuthContextExtractor.java`: 网关认证上下文提取器

#### eon-common-mybatis
**功能**: MyBatis增强组件

**主要特性**:
- MyBatis Plus集成
- 通用Mapper配置
- 分页插件支持
- 数据权限控制
- 自动填充配置

#### eon-common-datasource
**功能**: 动态数据源支持

**主要特性**:
- 多数据源配置
- 数据源切换注解 (`@UseDataSource`)
- 动态路由数据源
- 数据源健康检查
- 读写分离支持

**关键文件**:
- `DynamicDataSourceAutoConfiguration.java`: 动态数据源自动配置
- `DynamicRoutingDataSource.java`: 动态路由数据源
- `UseDataSource.java`: 数据源使用注解
- `DynamicDataSourceAspect.java`: 数据源切面

#### eon-common-feign
**功能**: Feign客户端增强

**主要特性**:
- 头部传播拦截器
- 统一Feign配置
- 服务间调用日志
- 熔断降级处理
- 负载均衡配置

**关键文件**:
- `HeaderPropagationRequestInterceptor.java`: 头部传播请求拦截器
- `EonFeignProperties.java`: Feign配置属性
- `EonFeignAutoConfiguration.java`: Feign自动配置

#### eon-common-log
**功能**: 日志组件

**主要特性**:
- 链路追踪ID生成
- 请求日志记录
- 日志切面支持
- 结构化日志
- 日志配置管理

**关键文件**:
- `TraceIdFilter.java`: 链路追踪ID过滤器
- `TraceIdProperties.java`: 追踪ID配置属性
- `LoggingAutoConfiguration.java`: 日志自动配置

#### eon-common-oss
**功能**: 文件上传组件

**主要特性**:
- 多OSS服务支持
- 统一文件上传接口
- 文件存储管理
- 文件访问控制

#### eon-common-seata
**功能**: 分布式事务组件

**主要特性**:
- Seata集成
- 分布式事务管理
- 事务拦截器
- 全局事务配置

#### eon-common-websocket
**功能**: WebSocket组件

**主要特性**:
- WebSocket配置
- 消息处理器
- 会话管理
- 广播支持

#### eon-common-swagger
**功能**: 接口文档组件

**主要特性**:
- Swagger自动配置
- API文档生成
- 接口测试支持
- 文档权限控制

**关键文件**:
- `SwaggerAutoConfiguration.java`: Swagger自动配置
- `SwaggerProperties.java`: Swagger配置属性

#### eon-common-xss
**功能**: XSS防护组件

**主要特性**:
- XSS过滤器
- 输入验证
- 输出编码
- 安全防护规则

### 关键代码示例

#### 统一返回结果 (R.java)
```java
@Data
@Accessors(chain = true)
@FieldNameConstants
public class R<T> implements Serializable {
    
    @Getter
    @Setter
    private int code;
    
    @Getter
    @Setter
    private String msg;
    
    @Getter
    @Setter
    private T data;
    
    public static <T> R<T> ok() {
        return restResult(null, CommonConstants.SUCCESS, null);
    }
    
    public static <T> R<T> ok(T data) {
        return restResult(data, CommonConstants.SUCCESS, null);
    }
    
    public static <T> R<T> failed() {
        return restResult(null, CommonConstants.FAIL, null);
    }
    
    public static <T> R<T> failed(String msg) {
        return restResult(null, CommonConstants.FAIL, msg);
    }
    
    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
```

#### 用户上下文管理
```java
public class UserContextHolder {
    private static final ThreadLocal<AuthenticatedUser> CONTEXT = new ThreadLocal<>();
    
    public static void setContext(AuthenticatedUser user) {
        CONTEXT.set(user);
    }
    
    public static AuthenticatedUser getContext() {
        return CONTEXT.get();
    }
    
    public static void clear() {
        CONTEXT.remove();
    }
}
```

#### 当前用户注解
```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
```

#### 动态数据源注解
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UseDataSource {
    String value() default "master";
}
```

### 依赖配置

#### 父级POM配置
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.eon</groupId>
        <artifactId>eon</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>eon-common</artifactId>
    <packaging>pom</packaging>
    <name>eon-common</name>
    <description>EON公共组件库</description>
    
    <modules>
        <module>eon-common-bom</module>
        <module>eon-common-core</module>
        <module>eon-common-security</module>
        <module>eon-common-mybatis</module>
        <module>eon-common-datasource</module>
        <module>eon-common-feign</module>
        <module>eon-common-log</module>
        <module>eon-common-oss</module>
        <module>eon-common-seata</module>
        <module>eon-common-websocket</module>
        <module>eon-common-swagger</module>
        <module>eon-common-xss</module>
    </modules>
</project>
```

### 使用示例

#### 在业务模块中使用公共组件
```xml
<dependencies>
    <dependency>
        <groupId>com.eon</groupId>
        <artifactId>eon-common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eon</groupId>
        <artifactId>eon-common-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.eon</groupId>
        <artifactId>eon-common-datasource</artifactId>
    </dependency>
</dependencies>
```

#### 使用统一返回结果
```java
@RestController
public class UserController {
    
    @GetMapping("/users/{id}")
    public R<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getById(id);
        return R.ok(user);
    }
    
    @PostMapping("/users")
    public R<UserDTO> createUser(@RequestBody UserCreateDTO dto) {
        UserDTO user = userService.create(dto);
        return R.ok(user);
    }
}
```

#### 使用当前用户注解
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    public R<UserMeResponse> me(@CurrentUser AuthenticatedUser currentUser) {
        UserMeResponse response = userService.getUserMe(currentUser.userId());
        return R.ok(response);
    }
}
```

#### 使用动态数据源
```java
@Service
public class UserService {
    
    @UseDataSource("slave")
    public UserDTO getUserFromSlave(Long id) {
        // 从从库查询用户
        return userMapper.selectById(id);
    }
    
    @UseDataSource("master")
    public void updateUser(UserDTO user) {
        // 更新主库用户
        userMapper.updateById(user);
    }
}
```

### 测试和质量

#### 测试文件覆盖
- `GatewayAuthContextExtractorTest.java`: 网关认证上下文提取器测试
- `DynamicDataSourceAspectTest.java`: 动态数据源切面测试
- `HeaderPropagationRequestInterceptorTest.java`: 头部传播拦截器测试
- `TraceIdFilterTest.java`: 链路追踪过滤器测试
- `SwaggerAutoConfigurationTest.java`: Swagger自动配置测试

#### 测试特点
- 单元测试覆盖核心功能
- 集成测试验证组件间协作
- 自动化测试确保代码质量
- 持续集成支持

### 重要文件列表

#### 核心文件
```
eon-common/
├── pom.xml                                    # 父级POM
├── eon-common-bom/
│   └── pom.xml                                # 依赖管理BOM
├── eon-common-core/
│   ├── src/main/java/com/eon/common/core/
│   │   ├── R.java                             # 统一返回结果
│   │   ├── constant/
│   │   │   ├── ServiceNameConstants.java      # 服务名称常量
│   │   │   ├── CommonConstants.java           # 通用常量
│   │   │   ├── SecurityConstants.java         # 安全常量
│   │   │   └── CacheConstants.java            # 缓存常量
│   │   └── constant/enums/                    # 枚举定义
│   └── pom.xml
├── eon-common-security/
│   ├── src/main/java/com/eon/common/security/
│   │   ├── context/
│   │   │   ├── UserContextHolder.java        # 用户上下文持有者
│   │   │   ├── AuthenticatedUser.java        # 认证用户信息
│   │   │   ├── CurrentUser.java               # 当前用户注解
│   │   │   ├── CurrentUserArgumentResolver.java # 参数解析器
│   │   │   ├── GatewayAuthContextExtractor.java # 网关认证上下文提取器
│   │   │   └── AuthContextAutoConfiguration.java # 认证上下文自动配置
│   │   ├── constant/
│   │   │   └── AuthHeaderConstants.java       # 认证头常量
│   │   └── context/UserPermissionsInterceptor.java # 用户权限拦截器
│   └── pom.xml
├── eon-common-datasource/
│   ├── src/main/java/com/eon/common/datasource/
│   │   ├── config/DynamicDataSourceAutoConfiguration.java # 动态数据源配置
│   │   ├── annotation/UseDataSource.java      # 数据源使用注解
│   │   ├── support/DynamicRoutingDataSource.java # 动态路由数据源
│   │   ├── support/DynamicDataSourceAspect.java # 数据源切面
│   │   └── properties/DynamicDataSourceProperties.java # 数据源配置属性
│   └── pom.xml
├── eon-common-feign/
│   ├── src/main/java/com/eon/common/feign/
│   │   ├── support/HeaderPropagationRequestInterceptor.java # 头部传播拦截器
│   │   ├── properties/EonFeignProperties.java # Feign配置属性
│   │   └── config/EonFeignAutoConfiguration.java # Feign自动配置
│   └── pom.xml
├── eon-common-log/
│   ├── src/main/java/com/eon/common/log/
│   │   ├── filter/TraceIdFilter.java          # 链路追踪过滤器
│   │   ├── properties/TraceIdProperties.java  # 追踪ID配置属性
│   │   └── config/LoggingAutoConfiguration.java # 日志自动配置
│   └── pom.xml
├── eon-common-swagger/
│   ├── src/main/java/com/eon/common/swagger/
│   │   ├── config/SwaggerAutoConfiguration.java # Swagger自动配置
│   │   └── properties/SwaggerProperties.java # Swagger配置属性
│   └── pom.xml
└── eon-common-xss/
    ├── src/main/java/com/eon/common/xss/     # XSS防护组件
    └── pom.xml
```

#### 测试文件
```
src/test/java/com/eon/common/
├── security/context/GatewayAuthContextExtractorTest.java
├── datasource/support/DynamicDataSourceAspectTest.java
├── feign/support/HeaderPropagationRequestInterceptorTest.java
├── log/filter/TraceIdFilterTest.java
└── swagger/config/SwaggerAutoConfigurationTest.java
```

### 变更日志 (Changelog)

### v2.0.0 (2025-09-21)
- **AI上下文深度初始化**: 完成公共组件模块的深度分析和文档更新
- **组件架构完善**: 详细描述12个子模块的功能和关键文件
- **核心代码示例**: 提供关键组件的代码示例和使用说明
- **集成测试增强**: 完善各组件的测试覆盖和质量保证
- **依赖关系明确**: 清晰定义各组件间的依赖关系和使用方式

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立公共组件库文档
- **核心功能**: 提供统一的基础设施和工具类
- **模块完善**: 建立完整的公共组件体系

---

**模块路径**: `/home/pig/github/eon-github/eon-common`  
**维护状态**: 持续开发中  
**技术栈**: Spring Boot + Spring Cloud + 各种中间件客户端  
**主要功能**: 统一基础设施、工具类、安全组件、数据访问组件等12个子模块