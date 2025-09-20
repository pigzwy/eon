# eon-gateway 网关服务

> 角色：流量入口与安全边界，负责统一路由、认证校验、限流与灰度发布。

## 1. 核心特性
- **反应式处理**：基于 Spring Cloud Gateway + WebFlux，支持高并发与背压控制。
- **认证透传**：校验来自 `eon-auth` 的 JWT，验证通过后生成 `X-User-*`、`X-Roles`、`X-Policy-Version` 等头给后端；失败时返回 401/403。
- **动态路由**：默认以配置文件静态路由示例，推荐在 Nacos `gateway-routes` 配置中维护；支持基于 metadata 的角色校验。
- **弹性治理**：整合 Resilience4j 断路器、重试、请求限流（RedisRateLimiter），保护后端服务稳定。
- **链路追踪**：预留 TraceId/SpanId 填充点，可对接 SkyWalking、Zipkin 或 Sleuth。

## 2. 运行参数
- **默认端口**：`3100`
- **依赖组件**：Redis（限流必需）、Nacos（注册配置）、授权服务（JWT 校验）、Prometheus（可选指标上报）。
- **关键配置**：`AUTH_SERVER_ISSUER_URI`、`AUTH_SERVER_JWKS_URI`、`NACOS_HOST`、`REDIS_HOST`、`gateway.security.whitelist`。

## 3. 启动步骤
```bash
# 已有 Redis + Nacos 场景
mvn -pl eon-gateway -am package -DskipTests
java -jar target/eon-gateway-*.jar --spring.profiles.active=dev

# 若需本地快速体验，可在 docker-compose 中补充 redis 与 nacos 服务
```

## 4. 已内置的过滤器
| 过滤器 | 说明 |
| ------ | ---- |
| `RemoveRequestHeader` | 清理下游可能带入的用户头，防止伪造 |
| `SaveSession` | 配合限流、鉴权等场景需要保存 Session |
| `RequestSize` | 限制上传体积，默认 10MB |
| `Retry` | 针对 GET 请求的 502/503 自动重试 |
| `CircuitBreaker` | 断路器 + fallback，默认兜底 `/__fallback/{service}` |
| `RequestRateLimiter` | 基于 Redis 的令牌桶限流，使用 `userKeyResolver` |

## 5. 与其他模块的协作
1. **eon-auth**：`gateway.security.jwksUri`、`issuer` 指向认证服务；若启用 mTLS，请配置证书到 `WebClient`。
2. **eon-common-security**：透传头由网关生成，下游服务通过自动装配的过滤器解析，无需额外配置。
3. **业务服务**：通过 `metadata.requiredRoles` 声明所需角色，后续可扩展统一策略引擎。

## 6. 推荐增强项
- **动态路由管理后台**：结合配置中心或数据库维护路由规则，提供灰度、权重路由、A/B 测试能力。
- **统一认证过滤器**：将 JWT 验证抽象为定制 `GlobalFilter`，支持多租户、会话黑名单等高级策略。
- **观测性**：
  - 导出 `/actuator/gateway/routes`、`/actuator/metrics/gateway.*`；
  - 接入分布式追踪与日志注入（TraceId / UserId）。
- **安全**：结合 Web Application Firewall（WAF）或 Bot 管理；对敏感端点进行二次校验（验证码/风控）。

## 7. 调试指引
```bash
# 生成访问令牌
./../eon-auth/test-password-grant.sh

# 携带 JWT 调用业务接口
curl -H "Authorization: Bearer $TOKEN" http://localhost:3100/api/users/me
```

若返回 401，请检查：
1. `issuer` 与 `jwksUri` 是否与认证服务一致；
2. Redis 是否可用；
3. `gateway.security.whitelist` 是否覆盖当前路径。
