# eon-upms 聚合工程

> 角色：企业级用户权限管理（User Permission Management Service）聚合，包含 API 契约与业务实现。

## 1. 模块划分
| 模块 | 说明 | 依赖 |
| ---- | ---- | ---- |
| `eon-upms-api` | 暴露给其他服务的 Feign 接口、DTO、常量等；后续可发布为独立 Jar | `eon-common-core`、`eon-common-feign` |
| `eon-upms-biz` | 具体服务实现，负责用户/角色/权限等业务逻辑，端口默认 3200 | `eon-common-security`、`spring-boot-starter-web` 等 |

## 2. 使用场景
- 作为独立微服务部署，与 `eon-gateway`、`eon-auth` 协同实现完整 RBAC；
- 作为单体模块引入 `eon-boot`；
- 将 `eon-upms-api` 发布到私有 Maven 仓库，供其他业务项目引用接口与 DTO。

## 3. 构建命令
```bash
# 编译 API + 业务模块
mvn -pl eon-upms/eon-upms-biz -am package -DskipTests

# 若仅发布 API
mvn -pl eon-upms/eon-upms-api install -DskipTests
```

## 4. 后续迭代建议
- `eon-upms-api`：引入统一的错误码枚举、响应包装、Feign 拦截器；
- `eon-upms-biz`：补足领域实体、仓储、应用服务；与 `eon-user` 模块合并或协调职责，避免重复造轮子；
- 集成 `eon-common-security` 的零配置 `@CurrentUser` 能力，验证在 RBAC 接口中的注入效果。
