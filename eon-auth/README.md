# eon-auth 服务说明

> 角色：统一认证中心，负责 OAuth2 / OpenID Connect 授权流程与令牌签发。

## 1. 核心能力
- **标准协议支持**：基于 Spring Authorization Server，默认开启授权码、客户端凭证、密码模式与刷新令牌；可选启用设备码等扩展授权类型。
- **JWT 令牌服务**：发行、校验、撤销访问令牌与刷新令牌，支持自定义 Claim 与长期签名密钥管理。
- **客户端与用户管理**：集成 Spring Security，支持内置用户、数据库用户或外部用户源；客户端配置支持动态加载。
- **Nacos 集成**：注册到服务发现列表，同时可从配置中心拉取 `application-*.yml` 补充敏感配置。
- **统一密码策略**：默认复用 `eon-common-security` 暴露的 `BCryptPasswordEncoder`，可通过声明同名 Bean 覆盖。

## 2. 运行参数
- **默认端口**：`3000`
- **关键环境变量**：`MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`AUTH_SERVER_ISSUER_URI`、`NACOS_HOST`。
- **依赖组件**：MySQL（OAuth 客户端与授权存储）、Redis（令牌缓存，可选）、Nacos（注册与配置）。
- **健康检查**：`GET /actuator/health`；`/actuator/info`、`/actuator/metrics` 可结合 Prometheus。 

## 3. 启动指南
```bash
# 数据库初始化（如本地快速体验）
mysql -h127.0.0.1 -uroot -proot < src/main/resources/sql/schema.sql
mysql -h127.0.0.1 -uroot -proot < src/main/resources/sql/data.sql

# 构建与运行
mvn -pl eon-auth -am package -DskipTests
java -jar target/eon-auth-*.jar --spring.profiles.active=dev
```

> 若需要一次性拉起依赖，可扩展 `docker-compose`：MySQL、Redis、Nacos、Keycloak（测试 OIDC 互通）等。

## 4. 与其他模块的协作
1. **eon-gateway**：验证 JWT 并透传 `X-User-*` 头；失败时回调 `/oauth2/token`、`/oauth2/introspect`。
2. **eon-user / eon-upms**：作为用户信息源，认证服务通过 JPA/Feign 读取用户、角色、权限；生成令牌时附带 `uid`、`tenantId`、`policyVersion` 等 Claim。
3. **eon-common-security**：下游服务依赖该模块解析透传头；认证服务自身也重用统一的密码编码与用户模型。

## 5. 可扩展性建议
- **多租户场景**：在 `OAuth2PasswordAuthenticationProvider` 中加入租户校验，并在令牌 Claim 中注入 `tenant_id`。
- **客户端管理后台**：结合 `eon-visual/eon-codegen` 或单独前端构建，提供动态 CRUD；数据仍落地至 MySQL。
- **安全加固**：
  - 启用 HTTPS 与 mTLS；
  - 对敏感端点（`/oauth2/jwks`、`/oauth2/introspect`）添加网关白名单；
  - 使用外部 KMS 管理签名密钥。
- **可观测性**：配置 `management.endpoints.web.exposure.include=health,info,metrics,loggers`，并集成链路追踪（SkyWalking/Zipkin）。

## 6. 常见问题
| 场景 | 排查建议 |
| ---- | -------- |
| Token 校验失败 | 确认 `issuer`、`jwks` 地址与网关配置一致；检查系统时间是否漂移|
| 客户端不允许密码模式 | 在数据库或配置中心为客户端开启 `password` 授权类型，并设置受信任范围 |
| Redis 未启用 | 若本地无 Redis，可在配置中心关闭 Redis 相关配置（使用内存存储），但不推荐生产使用 |

## 7. 下一步路线图
- 补充 `token introspection` 与 `device code` 模式集成测试。
- 引入客户端注册接口，实现自助式客户端管理。
- 与审计系统打通，将成功/失败登录事件统一落地。
