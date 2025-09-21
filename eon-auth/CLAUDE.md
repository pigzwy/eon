# [Root Directory](../../CLAUDE.md) > [eon-common](../) > **eon-auth**

## EON 授权服务模块

### 模块职责

EON授权服务模块是基于Spring Security OAuth2 Authorization Server的企业级认证授权服务，提供OAuth2.0标准协议的授权服务器功能，支持多种授权模式和JWT令牌管理。

### 入口和启动

**启动类**: `EonAuthApplication`
- **包路径**: `com.eon.auth.EonAuthApplication`
- **端口配置**: 3000
- **启动命令**: `java -jar eon-auth/target/*.jar`

**核心注解**:
- `@SpringBootApplication`: Spring Boot应用主类
- `@EnableConfigurationProperties`: 启用配置属性绑定

### 外部接口

**OAuth2标准端点**:
```yaml
授权端点:
  - GET /oauth2/authorize - OAuth2授权端点
  - POST /oauth2/token - 令牌获取端点
  - POST /oauth2/introspect - 令牌验证端点
  - POST /oauth2/revoke - 令牌撤销端点

JWK端点:
  - GET /oauth2/jwks - JSON Web Key Set端点

OpenID Connect端点:
  - GET /.well-known/oauth-authorization-server - 服务器配置信息
  - GET /userinfo - 用户信息端点

健康检查:
  - GET /actuator/health - 健康状态检查
  - GET /actuator/info - 应用信息
  - GET /actuator/metrics - 指标数据
```

### 关键依赖和配置

**Maven依赖**:
```xml
<!-- Web和安全基础 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- OAuth2授权服务器 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-authorization-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
</dependency>

<!-- 数据访问 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<dependency>
    <groupId>com.eon</groupId>
    <artifactId>eon-common-datasource</artifactId>
</dependency>

<!-- 缓存支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 服务发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

**配置文件** (`application.yml`):
```yaml
server:
  port: 3000

spring:
  application:
    name: eon-auth
  jpa:
    hibernate:
      ddl-auto: none
    defer-datasource-initialization: true
  sql:
    init:
      mode: never
      schema-locations: classpath:sql/schema.sql
      data-locations: classpath:sql/data.sql
  data:
    redis:
      host: ${REDIS_HOST:127.0.0.1}
      port: ${REDIS_PORT:6379}
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_HOST:127.0.0.1}:${NACOS_PORT:8848}
      config:
        server-addr: ${spring.cloud.nacos.discovery.server-addr}

# OAuth2配置
security:
  oauth2:
    authorization-server:
      issuer: ${AUTH_SERVER_ISSUER_URI:http://localhost:3000}

# EON自定义配置
eon:
  oauth2:
    client:
      default:
        client-id: ${OAUTH2_CLIENT_ID:eon-console}
        client-secret: ${OAUTH2_CLIENT_SECRET:console-secret}
        grant-types:
          - authorization_code
          - refresh_token
          - client_credentials
          - password
        redirect-uris:
          - http://127.0.0.1:8080/login/oauth2/code/eon
          - http://localhost:9999/authorized
        scopes:
          - openid
          - profile
          - read
          - write
```

### 核心配置类

**AuthorizationServerConfig**:
- 配置OAuth2授权服务器
- 设置JWT签名密钥
- 配置客户端详情服务
- 注册授权模式

**DefaultSecurityConfig**:
- Spring Security安全配置
- 配置安全过滤链
- 设置认证和授权规则

**FederatedIdentityIdTokenCustomizer**:
- 自定义ID Token生成器
- 支持联邦身份集成

### 支持的授权模式

**授权码模式 (Authorization Code)**:
- 最安全的授权模式
- 适用于Web应用
- 支持PKCE增强安全性

**客户端凭证模式 (Client Credentials)**:
- 适用于服务间调用
- 无需用户参与
- 机器对机通信

**密码模式 (Resource Owner Password)**:
- 适用于受信任的应用
- 直接使用用户名密码
- 逐渐被不推荐使用

**刷新令牌模式 (Refresh Token)**:
- 用于获取新的访问令牌
- 延长用户会话
- 减少重复认证

### 数据模型

**OAuth2相关表**:
```sql
-- OAuth2客户端表
CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_id_issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_secret VARCHAR(200),
    client_secret_expires_at TIMESTAMP,
    client_name VARCHAR(200),
    client_authentication_methods VARCHAR(1000),
    authorization_grant_types VARCHAR(1000),
    redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000),
    client_settings VARCHAR(2000),
    token_settings VARCHAR(2000)
);

-- OAuth2授权表
CREATE TABLE oauth2_authorization (
    id VARCHAR(100) PRIMARY KEY,
    registered_client_id VARCHAR(100),
    principal_name VARCHAR(200),
    authorization_grant_type VARCHAR(100),
    attributes VARCHAR(4000),
    state VARCHAR(500),
    authorization_code_value VARCHAR(4000),
    authorization_code_issued_at TIMESTAMP,
    authorization_code_expires_at TIMESTAMP,
    authorization_code_metadata VARCHAR(2000),
    access_token_value VARCHAR(4000),
    access_token_issued_at TIMESTAMP,
    access_token_expires_at TIMESTAMP,
    access_token_metadata VARCHAR(2000),
    access_token_type VARCHAR(100),
    access_token_scopes VARCHAR(1000),
    oidc_id_token_value VARCHAR(4000),
    oidc_id_token_issued_at TIMESTAMP,
    oidc_id_token_expires_at TIMESTAMP,
    oidc_id_token_metadata VARCHAR(2000),
    refresh_token_value VARCHAR(4000),
    refresh_token_issued_at TIMESTAMP,
    refresh_token_expires_at TIMESTAMP,
    refresh_token_metadata VARCHAR(2000),
    user_code_value VARCHAR(100),
    user_code_issued_at TIMESTAMP,
    user_code_expires_at TIMESTAMP,
    user_code_metadata VARCHAR(2000),
    device_code_value VARCHAR(100),
    device_code_issued_at TIMESTAMP,
    device_code_expires_at TIMESTAMP,
    device_code_metadata VARCHAR(2000)
);

-- OAuth2授权同意表
CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100),
    principal_name VARCHAR(200),
    authorities VARCHAR(1000),
    PRIMARY KEY (registered_client_id, principal_name)
);
```

### 测试和质量

**测试文件**:
- `EonAuthApplicationTests.java`: 应用启动测试
- `PasswordAuthenticationTests.java`: 密码认证测试
- `BcryptOnceTest.java`: 密码加密测试
- `FederatedIdentityIdTokenCustomizerTest.java`: ID Token自定义测试

**测试覆盖**:
- OAuth2授权流程测试
- JWT令牌生成和验证测试
- 密码认证提供者测试
- 联邦身份集成测试

### 重要文件列表

**核心文件**:
```
src/main/java/com/eon/auth/
├── EonAuthApplication.java                    # 启动类
├── config/
│   ├── AuthorizationServerConfig.java         # OAuth2授权服务器配置
│   ├── DefaultSecurityConfig.java            # Spring Security配置
│   ├── AuthorizationServerTokenProperties.java # 令牌配置属性
│   └── FederatedIdentityIdTokenCustomizer.java # ID Token自定义
├── support/
│   ├── password/                             # 密码认证支持
│   │   ├── PasswordAuthenticationConverter.java
│   │   ├── PasswordAuthenticationToken.java
│   │   └── PasswordAuthenticationProvider.java
│   ├── security/                             # 安全支持
│   │   └── JdbcUserDetailsServiceAdapter.java
│   └── user/                                 # 用户权限服务
│       └── UserAuthorityService.java
└── FederatedIdentityAuthenticationSuccessHandler.java # 联邦身份成功处理器

src/main/resources/
├── application.yml                           # 应用配置
├── sql/
│   ├── schema.sql                            # 数据库表结构
│   └── data.sql                              # 初始化数据
└── logback-spring.xml                        # 日志配置

src/test/java/com/eon/auth/
├── EonAuthApplicationTests.java              # 应用测试
├── PasswordAuthenticationTests.java          # 密码认证测试
├── BcryptOnceTest.java                       # 密码加密测试
└── config/
    └── FederatedIdentityIdTokenCustomizerTest.java # ID Token测试
```

### 变更日志 (Changelog)

### v1.0.0 (2025-09-21)
- **OAuth2授权服务器**: 完整的Spring Security OAuth2 Authorization Server实现
- **多种授权模式**: 支持授权码、客户端凭证、密码、刷新令牌等标准模式
- **JWT令牌管理**: 支持JWT令牌的生成、验证和撤销
- **服务发现集成**: 集成Nacos服务注册发现和配置中心
- **数据库持久化**: 使用JPA持久化OAuth2客户端和授权数据
- **联邦身份支持**: 支持第三方身份提供商集成
- **完整的测试覆盖**: 包含授权流程、令牌管理、安全配置等测试

---

**模块路径**: `/home/pig/github/eon-github/eon-auth`  
**维护状态**: 持续开发中  
**技术栈**: Spring Boot 3.5.5 + Spring Security OAuth2 + Nacos + MySQL + Redis  
**主要功能**: OAuth2授权服务器、JWT令牌管理、用户认证