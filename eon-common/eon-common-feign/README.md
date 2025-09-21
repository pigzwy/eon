# eon-common-feign 模块说明

> 目标：统一远程调用的基础配置，减少各服务重复编写 Feign 拦截器与超时设置。

## 核心能力
- **请求头透传**：默认携带认证、租户、权限与 TraceId 头信息，可通过 `eon.feign.headers` 自定义列表。
- **静态头注入**：支持在配置中声明固定请求头，例如服务标识、版本号。
- **Trace 兜底**：当线程无 HTTP 上下文时，可从 MDC 中读取链路 ID 继续透传。
- **统一超时**：通过 `eon.feign.connect-timeout`、`eon.feign.read-timeout` 控制请求超时，默认 3s/5s。
- **日志级别**：`eon.feign.log-level` 控制 Feign 日志颗粒度，默认 `BASIC`。

## 快速使用
1. 依赖引入：业务模块只需同时引入 `spring-cloud-starter-openfeign` 与本模块（通常通过聚合 POM 自动完成）。
2. 开启配置项（可选）：
   ```yaml
   eon:
     feign:
       headers:
         - Authorization
         - X-User-Id
         - X-Tenant-Id
         - X-Trace-Id
       static-headers:
         X-App-Name: eon-user
       connect-timeout: 2s
       read-timeout: 4s
       log-level: FULL
   ```
3. 如果某环境不需要透传，可在配置中关闭：
   ```yaml
   eon:
     feign:
       header-propagation-enabled: false
   ```

## 扩展建议
- 若需自定义错误解码或重试策略，可在业务工程中定义 `ErrorDecoder`、`Retryer` Bean 覆盖默认实现。
- 后续计划沉淀与 Sentinel/Resilience4j 的熔断整合，敬请关注 `docs/IMPROVEMENT_PLAN.md`。
