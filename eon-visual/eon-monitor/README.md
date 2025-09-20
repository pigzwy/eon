# eon-monitor 服务说明

> 角色：集中展示各微服务的健康状态与运行指标，建议集成 Spring Boot Admin。

## 1. 功能规划
- 聚合各服务 `/actuator` 数据，提供可视化面板；
- 与告警系统（钉钉/飞书/邮件）联动；
- 支持服务上下线通知、版本对比。

## 2. 当前状态
- 仅包含启动类与基础配置，默认端口 `5001`；
- 尚未引入 Spring Boot Admin 依赖，可按需补充。

## 3. 启动方式
```bash
mvn -pl eon-visual/eon-monitor -am package -DskipTests
java -jar target/eon-monitor-*.jar
```

## 4. 待办事项
1. 引入 `spring-boot-admin-starter-server` 与前端静态资源；
2. 在网关上配置安全访问策略；
3. 配置 `spring.boot.admin.notify`，对接告警渠道。
