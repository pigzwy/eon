# eon-common-security 模块说明

> 目标：在所有后端服务中提供一套统一、可复用的“当前用户上下文”能力，解决每个控制器重复解析网关头或 JWT 的痛点。

## 1. 模块定位

- **所属层级**：`eon-common` 聚合工程下的公共安全组件。
- **提供能力**：
  1. 解析网关透传的认证头（用户 ID、租户、角色、权限、策略版本、TraceId 等）。
  2. 在一次请求内，通过 `ThreadLocal + Request Attribute` 暴露 `AuthenticatedUser` 及权限列表。
  3. 自动解析 `X-User-Permissions` 头，写入 `UserPermissionsContext`。
  4. 通过 `@CurrentUser` 注解，让 Spring MVC 控制器参数自动注入当前用户信息。
  5. 默认提供 `BCryptPasswordEncoder` Bean，业务可按需覆盖。
  6. 采用 Spring Boot 自动装配，业务模块只需引入依赖即可开箱使用。

## 2. 目录结构与依赖关系

```
eon-common-security/
├── pom.xml                         # 模块依赖定义
├── README.md                       # 当前文档
├── src/main/java
│   └── com/eon/common/security
│       ├── constant
│       │   └── AuthHeaderConstants.java
│       └── context
│           ├── AuthenticatedUser.java
│           ├── UserContextHolder.java
│           ├── UserPermissionsContext.java
│           ├── GatewayAuthContextExtractor.java
│           ├── GatewayAuthContextFilter.java
│           ├── CurrentUser.java
│           ├── CurrentUserArgumentResolver.java
│           ├── UserPermissionsInterceptor.java
│           └── AuthContextAutoConfiguration.java
└── src/main/resources
    └── META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 核心类职责

| 类名 | 作用 | 依赖关系 |
| --- | --- | --- |
| `AuthenticatedUser` | 请求内用户快照记录，包括 `userId/tenantId/roles/...`。 | 无直接依赖，纯值对象。 |
| `UserContextHolder` | 使用 `ThreadLocal` 保存/清理 `AuthenticatedUser`，并提供从 `HttpServletRequest` 兜底获取的方法。 | 被过滤器与参数解析器调用。 |
| `GatewayAuthContextExtractor` | 从网关透传的 HTTP 头解析身份，兼容 `X-Roles` 与旧头 `X-User-Roles`。 | 依赖 `AuthHeaderConstants`，被过滤器调用。 |
| `GatewayAuthContextFilter` | `OncePerRequestFilter`，调用 `Extractor`，在过滤器链最前注入 `AuthenticatedUser` 并写入上下文。 | 注册在自动装配中。 |
| `CurrentUser` | 控制器参数注解。 | 被解析器识别。 |
| `CurrentUserArgumentResolver` | Spring MVC 参数解析器，支持 `@CurrentUser AuthenticatedUser` 注入。 | 使用 `UserContextHolder` 兜底。 |
| `UserPermissionsContext` | `ThreadLocal` 保存请求级权限列表。 | 提供给拦截器与业务代码。 |
| `UserPermissionsInterceptor` | MVC 拦截器，解析权限头并写入上下文及请求属性。 | 自动装配注册。 |
| `AuthContextAutoConfiguration` | Spring Boot 自动配置，注册过滤器、解析器、权限拦截器与默认 `PasswordEncoder`。 | 通过 `META-INF/...AutoConfiguration.imports` 自动生效。 |
| `AuthHeaderConstants` | 统一维护所有网关相关头名常量。 | 被 `Extractor` 等类引用。 |

### Bean 注册流程

1. **自动装配**：`AuthContextAutoConfiguration` 通过 `.imports` 自动加载。
2. **过滤器 Bean**：注册 `GatewayAuthContextFilter`，默认顺序 `Ordered.HIGHEST_PRECEDENCE + 50`。
3. **权限拦截器**：注册 `UserPermissionsInterceptor`，确保权限头被落地。
4. **参数解析器**：`CurrentUserArgumentResolver` 加入 MVC 解析器链。
5. **密码编码器**：默认提供 `BCryptPasswordEncoder`，仅在容器无其他实现时生效。

## 3. 运行时执行流程

1. 网关在认证成功后添加身份提示头（由 `eon-gateway` 负责）。
2. 下游服务在入口处由 `GatewayAuthContextFilter` 拦截：
   - 清理上一请求的上下文。
   - 调用 `GatewayAuthContextExtractor` 解析头，生成 `AuthenticatedUser`。
   - 将用户信息写入 `UserContextHolder`（ThreadLocal）和 `HttpServletRequest` 属性。
3. 业务控制器执行时：
   - 若方法参数标注 `@CurrentUser AuthenticatedUser`，则由 `CurrentUserArgumentResolver` 注入同一个对象。
   - 其他层可通过 `UserContextHolder.get()` 获取用户，通过 `UserPermissionsContext.getPermissions()` 获取权限。
4. 请求结束后：过滤器 `finally` 块调用 `UserContextHolder.clear()`，拦截器调用 `UserPermissionsContext.clear()`，防止 ThreadLocal 泄漏。

## 4. 快速使用指南

> 目标受众：编写新的下游服务或改造既有控制器的开发者。

1. **添加依赖**

   ```xml
   <dependency>
       <groupId>com.eon</groupId>
       <artifactId>eon-common-security</artifactId>
       <version>${project.version}</version>
   </dependency>
   ```

2. **控制器中使用**

   ```java
   @GetMapping("/me")
   public UserMeResponse me(@CurrentUser AuthenticatedUser currentUser) {
       Long userId = currentUser.userId();
       // 业务逻辑...
   }
   ```

3. **Service 层读取（可选）**

   ```java
   AuthenticatedUser user = UserContextHolder.get();
   if (user != null && user.hasRole("ADMIN")) {
       // …自定义业务逻辑
   }

   List<String> permissions = UserPermissionsContext.getPermissions();
   // …基于权限做精细化控制
   ```

4. **ID/租户兜底**：如果策略版本为空，可用业务数据兜底；`AuthenticatedUser` 中提供角色/权限集合，均为去重后的不可变 `Set`。

## 5. 可扩展/调整点

| 场景 | 建议操作 |
| --- | --- |
| 需要从 `SecurityContext`（JWT）补全更多 Claim | 在 `GatewayAuthContextExtractor.extract` 中增加读取逻辑，或新增一个 Decorator 在过滤器中组合。 |
| 定制过滤器顺序（如放在鉴权之后） | 在业务项目中定义 `FilterRegistrationBean<GatewayAuthContextFilter>` Bean，覆盖默认顺序。 |
| 增加更多传递字段（如部门、组织） | 更新 `AuthHeaderConstants`、`AuthenticatedUser`，同步扩展 `GatewayAuthContextExtractor`、权限拦截器及上下文，确保单元测试覆盖。 |
| 支持 WebFlux | 需单独实现 `WebFilter` 与 `HandlerMethodArgumentResolver` 的 WebFlux 版本，可放置在新包中并使用条件装配。 |
| 本地测试缺少头信息 | `CurrentUserArgumentResolver` 会抛出 `MissingRequestHeaderException`，可在 Mock 请求中手动添加必需头。 |

## 6. 常见问题（FAQ）

- **为什么不直接在控制器里读头？**
  - 控制器/Service 经常多次读取同样的头：存在重复、易错，且难以统一新增字段；使用统一上下文可实现“一处解析，多处复用”。

- **ThreadLocal 是否有内存泄漏风险？**
  - `GatewayAuthContextFilter` 在 `finally` 块清理，且过滤器顺序高于其他组件，确保不会残留。注意自定义过滤器在其之后执行时不要提前终止链路（早返回时也应调用 `clear`）。

- **能否配合 Spring Security 直接读取 `Authentication`？**
  - 模块当前只依赖 HTTP 头，未与 Spring Security 深度耦合。若未来启用 Resource Server，可在过滤器中优先读取 `SecurityContextHolder`，当作 TODO 扩展项。

## 7. 贡献与维护建议

1. **新增字段**：更新 `AuthenticatedUser`、`AuthHeaderConstants`、`GatewayAuthContextExtractor`、相应单测。
2. **扩展解析方式**：可按策略模式拆分多个 `Extractor` / 拦截器，在过滤器与拦截器中链式尝试。
3. **代码风格**：保持中文注释，遵守模块内格式；新增公共工具时补充单元测试。
4. **文档**：重大调整后请同步更新此 README，方便新成员迅速了解模块职责。

---
如需进一步优化（如结合 JWT Claims、支持异步线程池透传等），欢迎在该模块继续迭代。
