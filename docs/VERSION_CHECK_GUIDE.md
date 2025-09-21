 依赖版本冲突排查与校验指引

本文档说明如何在 eon 脚手架中识别和解决 Maven 依赖版本冲突，确保根 POM 与各业务模块依赖保持一致。

## 适用场景
- 引入新依赖或升级现有依赖后，需要确认不会引发传递版本飘移。
- 模块出现 `ClassNotFoundException`、`NoSuchMethodError` 等二进制兼容问题时，需要排查依赖树。
- CI 报告提示依赖冲突（dependency convergence error）或 BOM 版本未对齐。

## 快速检视流程
1. 执行 `mvn -q -DskipTests package`，确保基础构建无异常。
2. 运行 `mvn dependency:tree -Dincludes=com.example`（按需替换坐标）检视关键依赖来源。
3. 利用 `mvn help:effective-pom` 验证 BOM 与 `<dependencyManagement>` 是否生效。
4. 若发现冲突，优先在 `eon-common-bom` 中声明统一版本，再在业务模块通过 `<dependencyManagement>` 提供覆盖。

## 日常拉齐策略
- **新增依赖**：先评估是否已有 BOM 版本，若无则在 `eon-common-bom` 中声明并注明来源。
- **版本升级**：使用 `mvn versions:display-dependency-updates -pl 模块坐标` 获取候选版本，确认兼容性后更新 BOM。
- **局部测试**：对受影响模块执行 `mvn test -pl 模块 -am`，验证依赖升级未破坏单测。

## 常见问题定位
- **同坐标多版本**：在 `dependency:tree` 中查找 `(*)` 标记，定位冲突来源模块。
- **Spring Boot/Cloud 版本漂移**：确认 `<parent>` 与 `eon-common-bom` 引用的版本一致，避免直接写死子模块版本。
- **三方 BOM 覆盖**：若引入外部 BOM（如 Alibaba Cloud），需在根 POM 中控制导入顺序，保证自定义 BOM 优先生效。

## 提交前自检清单
- 相关依赖已在 `eon-common-bom` 中声明或用注释说明暂不纳入 BOM 的原因。
- 根 POM 不存在重复版本属性，`mvn help:effective-pom` 显示的版本与预期一致。
- 关键模块测试通过，并记录在 PR 描述中。
- 如有外部依赖升级，补充更新说明与回滚策略。
