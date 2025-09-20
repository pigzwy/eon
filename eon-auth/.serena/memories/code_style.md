# 代码风格与约定
- **语言风格**：主项目使用 Java；配置与注释以中文编写，强调业务含义与操作说明。
- **命名约定**：遵循 Spring/Java 惯例——类名 UpperCamelCase，方法与变量 lowerCamelCase，常量 UPPER_SNAKE_CASE。
- **安全配置**：密码散列统一通过 `BCryptPasswordEncoder`；客户端密钥入库前需编码。
- **配置管理**：OAuth2 客户端信息通过 `eon.oauth2.client.*` 属性外部化，支持环境变量覆盖；SQL 初始化脚本位于 `src/main/resources/sql/`。
- **测试脚本**：仓库提供 `test-oauth2-password.sh`、`test_password_auth.sh` 等 Bash 脚本，调用 curl/jq 进行端到端验证。