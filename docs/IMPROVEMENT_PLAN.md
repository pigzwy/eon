# 改进任务清单

以下清单按照优先顺序列出了当前脚手架需要完善的关键事项，每项均包含目标、范围与交付基准，后续改动将以此为准。

1. **依赖与版本管理统一** ✅  
   - 目标：去除根 `pom.xml` 中占位版本冲突，统一依赖来源并绑定 `eon-common-bom`。  
   - 范围：整理 `<properties>` 与 `<dependencyManagement>`，补充版本校验说明。  
   - 交付基准：确保 `mvn -q -DskipTests package` 可稳定运行且版本信息清晰。  
   - 进展：根 POM 现仅保留必要属性并 import `eon-common-bom`，BOM 补齐 mysql/druid 版本；新增 `docs/VERSION_CHECK_GUIDE.md` 作为版本冲突排查指引，交付基准已完成。

2. **补齐核心通用模块实现** ✅  
   - 目标：为 `eon-common-*` 中的占位模块提供最小可用功能或明确禁用策略。  
   - 范围：至少实现日志、数据源和 Swagger 基础能力；其他模块标注 TODO 与计划。  
   - 交付基准：模块具备基础配置与示例，`README`/注释清晰标识当前状态。  
   - 进展：`eon-common-log` 提供 TraceId 过滤器与 logback 模板，`eon-common-datasource` 支持注解驱动多数据源并在 `eon-auth`、`eon-user` 生效，`eon-common-swagger` 集成 springdoc，`eon-common-feign` 封装统一拦截器/日志/超时；新增 `eon-common/README.md` 列出其余通用模块的 TODO 与路线图，阶段目标达成。

3. **建立测试与覆盖率基线** ⏳  
   - 目标：引入统一的单测运行与覆盖率检测流程。  
   - 范围：配置 JaCoCo、补充关键安全链路的测试样例，初始化 GitHub Actions 流程。  
   - 交付基准：CI 能执行 `mvn verify` 并生成覆盖率报告，核心模块新增示例测试。  
   - 进展：根 POM 已启用 `jacoco-maven-plugin` 默认绑定，新增 `docs/TEST_COVERAGE_BASELINE.md` 说明执行规范，在 `eon-common-log`/`eon-common-feign`/`eon-common-security` 补充示例测试；仍需在 CI 环境完成依赖缓存与引入覆盖率阈值检查。

4. **完善本地与环境配置脚本**  
   - 目标：提升开发者一键启动体验。  
   - 范围：整理 `docker compose` 服务、初始化 SQL/配置脚本、示例 `application-*.yml`。  
   - 交付基准：提供分步骤指南，确保本地可快速启动注册中心及核心服务。

5. **安全与配置治理**  
   - 目标：制定统一的认证、授权、配置管理策略。  
   - 范围：补充 JWT、跨域、限流默认配置；规范 `.env` 使用与敏感信息隔离。  
   - 交付基准：安全策略文档化，并在关键模块中提供默认实现或 Hook。

6. **可观察性体系搭建**  
   - 目标：引入日志、指标、链路追踪的基础设施。  
   - 范围：统一日志格式、暴露 Prometheus 指标、预留 Zipkin/SkyWalking 集成点。  
   - 交付基准：核心服务具备基础日志配置与指标端点，文档说明接入方式。

7. **文档与运维指引扩展**  
   - 目标：完善架构、部署、故障排查等文档。  
   - 范围：补充架构图、服务依赖关系、常见问题与回滚方案。  
   - 交付基准：新增文档与现有 `README`、`AGENTS.md` 保持一致性，降低接入成本。
