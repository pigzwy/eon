# Repository Guidelines

## 项目结构与模块组织
仓库采用 Maven 多模块聚合结构，根 `pom.xml` 统一声明 Spring Boot 3.5.x、Spring Cloud 2025 与 Java 17。核心业务模块包括 `eon-gateway`（边界网关）、`eon-auth`（认证授权）、`eon-upms`（权限管理）、`eon-user`（业务示例）与 `eon-visual`（运维可视化），单体启动入口位于 `eon-boot`。通用能力沉淀在 `eon-common` 聚合模块，各子模块遵循 `src/main/java` 与 `src/main/resources` 布局，测试样例位于对应模块的 `src/test/java`。环境配置集中在 `application*.yml`，本地覆盖可复制 `.env.example` 为 `.env.local`，并使用 `META-INF/spec` 同步接口契约。

## 构建、测试与开发命令
`mvn -q -DskipTests package` 快速验证全量编译是否通过，日常联调建议执行 `mvn clean verify -pl eon-gateway -am` 以便同步依赖模块。针对单模块单测，可运行 `mvn test -pl eon-gateway` 或替换目标模块；提交前至少执行受影响模块的 `mvn test`。本地依赖（MySQL、Redis、Nacos）可通过 `eon-user/docker-compose.yml` 启动：`docker compose up -d`，停用时执行 `docker compose down -v` 释放资源。

## 编码风格与命名约定
Java 代码统一使用 4 空格缩进，类、接口、枚举采用 PascalCase，方法与变量遵循 camelCase，常量位于 `Constant` 或枚举类型并使用全大写加下划线。REST 路径坚持资源复数形式（如 `/api/users`），请求响应 DTO 分别以 `*Request`、`*Response` 结尾。YAML 键名使用小写加中划线，并用中文注释说明。优先通过 Lombok（例如 `@Slf4j`、`@RequiredArgsConstructor`）消除样板代码，确保与 Spring 注入模型保持一致。

## 测试规范
默认使用 JUnit Jupiter 与 Spring Test，响应式流程参考 `eon-gateway` 的 Reactor 断言写法。测试方法命名遵循 `should_行为描述`，复杂场景构建专用 Test Utility，避免依赖真实外部资源。需要加载 Spring 上下文时优先 `@SpringBootTest` + `@ActiveProfiles("test")` 的轻量配置。安全与鉴权路径需保持 80% 以上语句覆盖率，可结合 JaCoCo 报表持续跟踪。

## 提交与 Pull Request 指南
Git 历史偏好简短中文祈使句或 `type: 摘要` 风格，例如 `修复 Gateway 认证头`、`feat: 新增租户隔离`，控制在 50 字符内并避免调试输出混入。Pull Request 请包含变更目的、关键实现、影响模块清单与测试结果摘要；界面或协议调整需附示例请求或截图，并关联相关 Issue。提交前确认 `target/`、`.idea/` 等临时文件未被纳入版本库。

## 安全与配置提示
敏感凭据必须通过环境变量或配置中心注入，禁止写入仓库。基础设施端口、缓存或存储变更时同步更新 `README.md` 与运维剧本，并校验 `eon-user/docker-compose.yml` 的映射。跨模块调用使用 Spring Cloud OpenFeign 时，请在公共 API 子模块对齐 DTO 与错误码，必要时更新 `META-INF/spec` 以保持契约一致。