# EON User Service - RBAC 用户与权限目录服务

面向企业级架构的用户中心，提供用户、角色、权限、菜单等主数据管理能力，不再负责令牌签发，由认证服务（EON Auth Service / Spring Authorization Server）统一处理。

## 功能特点

- ✅ **标准 RBAC 模型**：用户-角色-权限三级绑定，可扩展多租户维度。
- ✅ **统一认证协同**：仅暴露内部 REST 接口，由网关透传 `X-User-*` 头部完成用户上下文识别。
- ✅ **策略缓存**：编译后的 API/菜单策略按用户缓存，支持 `policy_version` 驱动的自动失效。
- ✅ **审计友好**：角色/权限变更会自动提升用户策略版本号，指导客户端刷新令牌。
- ✅ **菜单聚合**：支持树形菜单编排与可见性控制，前端可直接消费。

## 技术栈

- **框架**：Spring Boot 3.5.5、Spring Data JPA、Spring Cloud Alibaba Nacos 2023.0.3.3
- **数据库**：MySQL 8.0（可按需替换为 TiDB / PolarDB）
- **缓存**：内存缓存（ConcurrentHashMap，可替换为 Caffeine/Redis）
- **密码算法**：BCrypt（同认证服务保持一致）

## 模块结构

```
eon-user/
├── config/
│   └── UserServiceConfiguration.java   # 密码器等公共 Bean
├── controller/
│   ├── HealthController.java           # 健康检查
│   ├── RoleController.java             # 角色与权限编排接口
│   └── UserController.java             # 用户 / 自身信息接口
├── dto/                                # 请求/响应模型
│   ├── CreateUserRequest.java
│   ├── UpdateUserRequest.java
│   ├── CreateRoleRequest.java
│   ├── AssignPermissionsRequest.java
│   ├── UserResponse.java / UserMeResponse.java
│   └── RoleResponse.java
├── entity/、repository/、service/      # 领域实体、仓储与服务实现
├── policy/CompiledPolicy.java          # 预编译权限策略
├── service/
│   ├── MenuService.java                # 菜单聚合
│   ├── PolicyService.java              # 策略构建与缓存
│   └── UserApplicationService.java     # 用户/角色应用层逻辑
└── resources/
    ├── sql/schema.sql + data.sql       # 数据脚本
    └── application.yml                 # 环境配置
```

## 关键接口（M0）

| 路径 | 方法 | 说明 | 备注 |
| ---- | ---- | ---- | ---- |
| `/users/me` | GET | 返回当前登录用户信息、角色、权限及菜单树 | 需网关注入 `X-User-Id`、`X-Policy-Version`、`X-Tenant-Id`|
| `/users/{id}` | GET | 按 ID 查询用户详情 | 管理操作，建议结合网关 RBAC 控制 |
| `/users` | POST | 创建用户并分配角色 | 密码会自动 BCryp 加密 |
| `/users/{id}` | PATCH | 更新邮箱、启用状态、密码、角色 | 改动角色/状态将提升策略版本 |
| `/roles` | GET | 查询角色列表（支持 `tenantId` 过滤） | |
| `/roles` | POST | 新增角色 | 可附带权限 ID 列表 |
| `/roles/{id}/permissions` | POST | 重置角色的权限集合 | 自动刷新关联用户策略版本 |

所有接口默认返回统一结构（示例）：

```json
{
  "id": 1,
  "username": "admin",
  "roles": ["admin"],
  "permissions": ["menu:system.users", "api:GET:/users/:id"],
  "policyVersion": 3
}
```

## 与认证 / 网关的协作

1. **认证服务**使用相同的 MySQL 数据库读取用户、角色、权限映射，并在 Access Token 中附带 `uid`、`tenant`、`pv`、`roles` 等 Claim。
2. **网关服务**验证 JWT 后将 `X-User-Id`、`X-Roles`、`X-Policy-Version`、`X-Tenant-Id` 等提示头注入下游请求，并根据路由 `metadata.requiredRoles` 做粗粒度校验；`X-User-Roles`/`X-User-Permissions` 仅作兼容，预计逐步下线。
3. **用户服务**接收到请求后，根据 `policy_version` 判断是否需要重建策略；角色/权限变更会提升用户 `policy_version` 并清理缓存，引导客户端刷新令牌。

## 当前用户上下文（方案 A 配套）

- 引入 `eon-common-security` 依赖后，无需手动读取 `X-User-*` 头部：
  ```java
  @GetMapping("/me")
  public UserMeResponse me(@CurrentUser AuthenticatedUser currentUser) {
      Long userId = currentUser.userId();
      // ...业务逻辑...
  }
  ```
- 需要在 Service 层获取用户信息时，可通过 `UserContextHolder.get()` 读取同一 `AuthenticatedUser` 快照，线程完成后会自动清理。
- 若旧代码仍依赖 `@RequestHeader("X-User-Roles")` 等写法，优先改为 `@CurrentUser`，确有特殊需求时请统一使用 `AuthHeaderConstants` 中的常量，避免硬编码头名。
- 新增角色/权限字段时，应同时更新：网关透传、`AuthHeaderConstants`、`AuthenticatedUser` 及相关单元测试。

## 快速启动

```bash
# 1. 依赖环境（MySQL + Redis + Nacos）
./start.sh

# 2. 编译
mvn -pl eon-user -am package -DskipTests

# 3. 启动（示例）
java -jar target/eon-user-*.jar --spring.profiles.active=dev
```

默认测试数据：

- 用户：`admin / admin123`
- 角色：`admin`
- 访问入口：`GET http://localhost:3001/users/me`（需经过网关带上 JWT）

## 后续扩展建议

- 将策略缓存替换为 Caffeine/Redis，支持多副本共享。
- 在角色/权限调整时推送领域事件（Kafka/NATS），供审计与实时同步使用。
- 补充 `/permissions` 查询、分页搜索等辅助接口。
- 根据业务需要补充多租户隔离（字段级或 schema 级）。
