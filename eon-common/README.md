# eon-common 模块概览

## 当前已交付能力
- **eon-common-bom**：统一声明 Spring Boot、Spring Cloud 及常用三方库版本，为业务模块提供单一依赖入口。
- **eon-common-core**：沉淀通用响应模型 `R` 与缓存、服务名等常量枚举，已在 `eon-user`、`eon-auth` 中使用。
- **eon-common-log**：提供带可控开关的 `TraceIdFilter` 与 logback 模板，支持沿用上游 TraceId 或自动生成并透传。
- **eon-common-datasource**：实现基于 `eon.datasource` 配置的动态多数据源，支持 `@UseDataSource` 注解切换并默认启用 HikariCP。
- **eon-common-swagger**：封装 springdoc-openapi 自动配置，可按 `eon.swagger.*` 自定义基础信息与多分组扫描范围。
- **eon-common-security**：提供网关认证头解析、`@CurrentUser` 注入、权限上下文与默认密码编码器，减少各服务重复解析逻辑。
- **eon-common-feign**：封装 Feign 日志级别、超时时间与请求头透传，默认携带认证/租户/TraceId，并支持静态头与 MDC 兜底。

## 待办模块规划
- **eon-common-mybatis**：目标是封装 MyBatis-Plus 插件（分页、通用审计字段）与代码生成器配置；需先确定是否复用动态数据源 Bean，计划在下一迭代补充基础分页与多租户拦截器。
- **eon-common-oss**：规划提供 MinIO/Aliyun OSS 的抽象适配层，默认基于 MinIO SDK；待梳理统一的凭据加载与 Bucket 命名规范，短期内先实现 MinIO Starter。
- **eon-common-seata**：负责注册 Seata GlobalTransaction 与数据源代理，需结合现有动态数据源判断代理顺序；计划在引入分布式事务前补充启停开关与示例配置。
- **eon-common-websocket**：定位为统一的 STOMP/WebSocket Server 封装，需落地 Spring Messaging 配置、会话鉴权与心跳策略；建议与 `eon-visual` 场景验证后再投入开发。
- **eon-common-xss**：提供全局 XSS 过滤器与白名单策略，计划在下一阶段结合 Gateway 层策略评估是否交给后端或前端处理，再决定具体实现。

## 后续动作建议
- 聚焦 `eon-common-mybatis` 的分页、多租户拦截器落地，与动态数据源保持兼容。
- 为 `eon-common-feign` 后续扩展错误解码、重试/熔断策略预留接口，并沉淀示例配置。
- 每次模块功能落地后补充最小示例或单测，并更新本文档状态列，保持与 `docs/IMPROVEMENT_PLAN.md` 同步。
- 若模块短期内不会启用（如 OSS、WebSocket），建议在 POM 中增加 `optional` 说明并在 README 保持计划更新，避免业务误用。
