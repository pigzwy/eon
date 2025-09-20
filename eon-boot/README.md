# eon-boot 单体网关/演示应用

> 角色：快速启动的单体示例，便于在未拆分微服务时验证业务或进行本地调试。

## 1. 定位
- 聚合常用 `starter`，直接暴露 REST 控制器进行演示；
- 注册到 Nacos，便于与微服务共存；
- 预留控制器示例，可替换为真实业务入口。

## 2. 配置参数
- **端口**：默认 `9999`（和网关冲突，请二选一启动）。
- **依赖**：`spring-cloud-starter-alibaba-nacos-discovery/config`、`eon-common-core`。
- **配置中心**：支持从 `nacos:eon-boot.yaml` 读取覆盖性配置。

## 3. 启动方式
```bash
mvn -pl eon-boot -am package -DskipTests
java -jar target/eon-boot-*.jar
# 访问 http://localhost:9999/ 查看示例 JSON
```

## 4. 常见用法
- 在业务尚未拆分时，将 MVC / Service 层代码放入该模块；
- 与 `eon-common-*` 公共依赖配合，快速验证工具类或安全组件；
- 作为前端联调时的 Mock 服务，通过 Nacos 暴露统一服务名。

## 5. 下一步建议
- 引入数据库/缓存配置模板，提供本地 profile；
- 接入 `eon-common-security` 演示 `@CurrentUser` 注入；
- 若作为真实单体应用使用，建议拆分模块并补齐测试/文档。
