# 测试与覆盖率基线

## 目标
- 统一使用 `mvn verify` 驱动单元测试与 JaCoCo 覆盖率统计。
- 提供最小示例测试帮助新模块快速对齐规范。
- 在 CI 中固定上传覆盖率报告，便于后续门禁策略接入。

## 本地执行指南
1. 默认命令：
   ```bash
   mvn verify -Dmaven.repo.local=./.m2
   ```
   > 推荐在项目根目录下自建本地仓库，避免依赖全局 `~/.m2` 权限。
2. 如需跳过测试可使用 `-DskipTests`，但在提交前应至少对受影响模块执行一次 `mvn test`。
3. 覆盖率报告位于各模块 `target/site/jacoco/index.html`，根目录会聚合所有模块的报告目录，CI 已通过 `jacoco-report` 工件保留结果。

## 样例与规范
- `eon-common-log` 模块已新增 `TraceIdFilterTest`，演示如何使用 `MockHttpServletRequest/Response` 验证过滤器逻辑，并提供链路上下文断言。
- 新增测试建议遵循：
  - 使用 JUnit Jupiter（`org.junit.jupiter.api.*`）。
  - 断言优先使用 AssertJ，保持语义化表达。
  - 方法命名建议 `should_行为` 或中文描述，与生产代码保持一致。
  - 对于线程上下文、静态工具类等，务必验证清理逻辑，防止后续扩展时出现资源泄漏。

## CI 配置说明
- `.github/workflows/ci.yml` 已在 `push` 与 `pull_request` 触发 `mvn -B verify`。
- `actions/upload-artifact` 会上传 `**/target/site/jacoco` 目录，可在 PR 页面直接下载覆盖率结果。
- 如需集成覆盖率阈值，可在后续迭代中为 `jacoco-maven-plugin` 添加 `check` goal，并在 `verify` 阶段执行。

## 下一步建议
- 为安全链路（如 `eon-common-security` 中的 `GatewayAuthContextFilter`）补充单元或 WebMvc 测试，覆盖核心鉴权逻辑。
- 当业务模块积累足够测试后，可引入 `jacoco:report-aggregate` 聚合报告，或接入 SonarQube 进行统一质量门禁。
