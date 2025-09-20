# eon-upms-api 公共接口模块

> 角色：为其他微服务提供统一的用户权限 RPC 契约，避免散落的 DTO/Feign 定义。

## 1. 模块内容
- 统一放置 DTO、VO、错误码、Feign Client 接口；
- 引入 `eon-common-core`（工具类、分页模型）与 `eon-common-feign`（Feign 拦截器、日志等）。

## 2. 建议规范
- **命名一致**：DTO/VO 以 `XXXRequest`、`XXXResponse` 结尾；
- **版本管理**：对外暴露的 Feign Client 通过 `@RequestMapping("/api/upms/v1")` 控制版本；
- **兼容策略**：新增字段确保向后兼容，必要时保留旧接口或提供默认值；
- **错误码**：维护统一枚举，配合 `BizException` 使用。

## 3. 发布方式
```bash
mvn -pl eon-upms/eon-upms-api install -DskipTests
# 上传到私服（示例）
mvn -pl eon-upms/eon-upms-api deploy -DskipTests
```

## 4. 协作说明
- 依赖方（如订单、支付服务）仅需引用此模块即可获得标准化 DTO 与 Feign Client；
- 若业务需要读取当前用户，可直接使用 `eon-common-security` 暴露的 `AuthenticatedUser`，避免重复封装。
