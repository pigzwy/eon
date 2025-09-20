[Root Directory](../../CLAUDE.md) > **eon-common**

# EON 公共组件模块

## 变更日志 (Changelog)

### v1.0.0 (2025-09-16)
- **模块初始化**: 建立公共组件库文档
- **核心功能**: 提供统一的基础设施和工具类
- **模块完善**: 建立完整的公共组件体系

## 模块职责

eon-common是EON框架的公共组件模块，提供统一的基础设施、工具类、安全组件、数据访问组件等，确保整个系统的一致性和可维护性。该模块采用分层设计，各个子模块职责明确，便于维护和扩展。

## 模块结构

```
eon-common/
├── eon-common-bom/          # 依赖管理BOM
│   └── pom.xml
├── eon-common-core/         # 核心工具类
│   ├── src/main/java/com/eon/common/core/
│   │   ├── Result.java      # 统一返回结果
│   │   ├── PageResult.java  # 分页结果
│   │   ├── ErrorCode.java   # 错误码定义
│   │   └── exception/       # 异常类
│   └── pom.xml
├── eon-common-security/     # 安全组件
│   ├── src/main/java/com/eon/common/security/
│   │   ├── util/           # 安全工具类
│   │   ├── config/         # 安全配置
│   │   └── annotation/     # 安全注解
│   └── pom.xml
├── eon-common-mybatis/      # MyBatis扩展
│   ├── src/main/java/com/eon/common/mybatis/
│   │   ├── config/         # MyBatis配置
│   │   ├── handler/        # 类型处理器
│   │   └── interceptor/    # 拦截器
│   └── pom.xml
├── eon-common-datasource/   # 动态数据源
│   ├── src/main/java/com/eon/common/datasource/
│   │   ├── config/         # 数据源配置
│   │   ├── aop/            # 数据源AOP
│   │   └── annotation/     # 数据源注解
│   └── pom.xml
├── eon-common-feign/        # Feign扩展
│   ├── src/main/java/com/eon/common/feign/
│   │   ├── config/         # Feign配置
│   │   ├── fallback/       # 降级处理
│   │   └── interceptor/    # 拦截器
│   └── pom.xml
├── eon-common-log/          # 日志组件
│   ├── src/main/java/com/eon/common/log/
│   │   ├── aspect/         # 日志切面
│   │   ├── config/         # 日志配置
│   │   └── annotation/     # 日志注解
│   └── pom.xml
├── eon-common-oss/          # 文件上传
│   ├── src/main/java/com/eon/common/oss/
│   │   ├── config/         # OSS配置
│   │   ├── service/        # OSS服务
│   │   └── enums/          # OSS类型枚举
│   └── pom.xml
├── eon-common-seata/        # 分布式事务
│   ├── src/main/java/com/eon/common/seata/
│   │   ├── config/         # Seata配置
│   │   └── interceptor/    # 事务拦截器
│   └── pom.xml
├── eon-common-websocket/    # WebSocket
│   ├── src/main/java/com/eon/common/websocket/
│   │   ├── config/         # WebSocket配置
│   │   ├── handler/        # 消息处理器
│   │   └── session/        # 会话管理
│   └── pom.xml
├── eon-common-swagger/      # 接口文档
│   ├── src/main/java/com/eon/common/swagger/
│   │   ├── config/         # Swagger配置
│   │   └── annotation/     # Swagger注解
│   └── pom.xml
├── eon-common-xss/          # XSS防护
│   ├── src/main/java/com/eon/common/xss/
│   │   ├── config/         # XSS配置
│   │   ├── filter/         # XSS过滤器
│   │   └── util/           # XSS工具类
│   └── pom.xml
└── pom.xml
```

## 核心组件说明

### eon-common-bom
**功能**: 统一依赖版本管理，确保各个模块使用相同的依赖版本

**主要特性**:
- 集中管理第三方依赖版本
- 避免版本冲突
- 便于版本升级

### eon-common-core
**功能**: 核心工具类和基础模型

**主要特性**:
- 统一返回结果封装 (`Result<T>`)
- 分页结果封装 (`PageResult<T>`)
- 错误码定义 (`ErrorCode`)
- 通用异常类
- 日期时间工具类
- 字符串工具类
- JSON工具类

### eon-common-security
**功能**: 安全相关组件

**主要特性**:
- 用户上下文工具类
- 权限验证工具类
- 加密解密工具类
- JWT令牌工具类
- 安全注解 (`@PreAuthorize`, `@Secured`)

### eon-common-mybatis
**功能**: MyBatis增强组件

**主要特性**:
- 通用Mapper接口
- 分页插件配置
- 逻辑删除支持
- 自动填充配置
- 数据权限拦截器

### eon-common-datasource
**功能**: 动态数据源支持

**主要特性**:
- 多数据源配置
- 数据源切换注解 (`@DataSource`)
- 读写分离支持
- 数据源健康检查

### eon-common-feign
**功能**: Feign客户端增强

**主要特性**:
- 统一Feign配置
- 服务间调用日志
- 熔断降级处理
- 负载均衡配置

## 关键代码示例

### 统一返回结果
```java
// eon-common-core/src/main/java/com/eon/common/core/Result.java
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.code = -1;
        r.message = msg;
        return r;
    }

    // getters and setters
}
```

### 分页结果
```java
public class PageResult<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    public static <T> PageResult<T> of(List<T> records, long total, long size, long current) {
        PageResult<T> result = new PageResult<>();
        result.records = records;
        result.total = total;
        result.size = size;
        result.current = current;
        result.pages = (total + size - 1) / size;
        return result;
    }

    // getters and setters
}
```

### 错误码定义
```java
public enum ErrorCode {
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(-1, "系统错误"),
    PARAM_ERROR(1001, "参数错误"),
    AUTH_ERROR(1002, "认证失败"),
    PERMISSION_ERROR(1003, "权限不足"),
    DATA_NOT_FOUND(2001, "数据不存在"),
    DATA_DUPLICATE(2002, "数据已存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // getters
}
```

### 通用异常类
```java
public class BusinessException extends RuntimeException {
    private int code;
    private String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    // getters
}
```

### 日期时间工具类
```java
public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }
}
```

## 依赖配置

### 父级POM配置
```xml
<!-- eon-common/pom.xml -->
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

### BOM配置
```xml
<!-- eon-common-bom/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.eon</groupId>
        <artifactId>eon-common</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>eon-common-bom</artifactId>
    <packaging>pom</packaging>
    <name>eon-common-bom</name>
    <description>EON公共组件依赖管理</description>
    
    <dependencyManagement>
        <dependencies>
            <!-- 内部依赖 -->
            <dependency>
                <groupId>com.eon</groupId>
                <artifactId>eon-common-core</artifactId>
                <version>${project.version}</version>
            </dependency>
            <dependency>
                <groupId>com.eon</groupId>
                <artifactId>eon-common-security</artifactId>
                <version>${project.version}</version>
            </dependency>
            <!-- 其他内部依赖 -->
            
            <!-- 第三方依赖 -->
            <dependency>
                <groupId>org.apache.commons</groupId>
                <artifactId>commons-lang3</artifactId>
                <version>3.12.0</version>
            </dependency>
            <dependency>
                <groupId>org.apache.commons</groupId>
                <artifactId>commons-collections4</artifactId>
                <version>4.4</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### 核心模块依赖
```xml
<!-- eon-common-core/pom.xml -->
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.eon</groupId>
        <artifactId>eon-common</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    
    <artifactId>eon-common-core</artifactId>
    <name>eon-common-core</name>
    <description>EON核心工具类</description>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
        </dependency>
    </dependencies>
</project>
```

## 使用示例

### 在业务模块中使用公共组件
```xml
<!-- 在业务模块的pom.xml中引入依赖 -->
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
        <artifactId>eon-common-mybatis</artifactId>
    </dependency>
</dependencies>
```

### 使用统一返回结果
```java
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/users/{id}")
    public Result<UserDTO> getUser(@PathVariable Long id) {
        UserDTO user = userService.getById(id);
        return Result.ok(user);
    }
    
    @PostMapping("/users")
    public Result<UserDTO> createUser(@RequestBody UserCreateDTO dto) {
        UserDTO user = userService.create(dto);
        return Result.ok(user);
    }
}
```

### 使用安全注解
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public Result<List<UserDTO>> listUsers() {
        List<UserDTO> users = userService.listUsers();
        return Result.ok(users);
    }
    
    @PreAuthorize("hasPermission('user', 'create')")
    @PostMapping("/users")
    public Result<UserDTO> createUser(@RequestBody UserCreateDTO dto) {
        UserDTO user = userService.create(dto);
        return Result.ok(user);
    }
}
```

## 测试和质量

### 单元测试
```java
@SpringBootTest
public class ResultTest {
    
    @Test
    public void testOkResult() {
        Result<String> result = Result.ok("success");
        assertEquals(0, result.getCode());
        assertEquals("ok", result.getMessage());
        assertEquals("success", result.getData());
    }
    
    @Test
    public void testFailResult() {
        Result<String> result = Result.fail("error");
        assertEquals(-1, result.getCode());
        assertEquals("error", result.getMessage());
        assertNull(result.getData());
    }
}
```

### 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CommonIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testCommonComponent() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
```

## 常见问题

### Q: 如何添加新的公共组件？
A: 在eon-common下创建新的子模块，实现相应的功能，并在父级POM中添加模块引用。

### Q: 如何处理版本冲突？
A: 使用eon-common-bom统一管理依赖版本，确保所有模块使用相同版本。

### Q: 如何扩展通用功能？
A: 在相应的子模块中添加新的工具类、配置类或注解，遵循现有代码风格。

### Q: 如何避免循环依赖？
A: 严格按照模块职责划分，确保依赖关系是单向的，避免相互依赖。

## 相关文件列表

### 核心文件
- `eon-common/pom.xml` - 公共组件父级POM
- `eon-common/eon-common-bom/pom.xml` - 依赖管理BOM
- `eon-common/eon-common-core/src/main/java/com/eon/common/core/Result.java` - 统一返回结果
- `eon-common/eon-common-core/src/main/java/com/eon/common/core/PageResult.java` - 分页结果
- `eon-common/eon-common-core/src/main/java/com/eon/common/core/ErrorCode.java` - 错误码定义

### 配置文件
- `eon-common/eon-common-core/src/main/resources/META-INF/spring.factories` - Spring自动配置
- `eon-common/eon-common-core/src/main/resources/application.yml` - 默认配置

### 测试文件
- `eon-common/eon-common-core/src/test/java/com/eon/common/core/ResultTest.java` - 返回结果测试
- `eon-common/eon-common-core/src/test/java/com/eon/common/core/ErrorCodeTest.java` - 错误码测试

## 扩展指南

### 添加新的工具类
1. 在对应的功能模块中创建工具类
2. 编写相应的单元测试
3. 更新文档说明
4. 发布版本更新

### 集成第三方库
1. 在BOM中添加依赖版本管理
2. 在对应模块中添加依赖
3. 封装第三方库的API
4. 提供统一的接口

### 性能优化
1. 优化工具类算法
2. 添加缓存机制
3. 减少内存分配
4. 优化线程安全

### 文档完善
1. 添加API文档
2. 编写使用示例
3. 完善错误处理
4. 更新版本记录

---

**更新时间**: 2025-09-16 10:40:53  
**文档版本**: v1.0.0  
**维护状态**: 持续更新中