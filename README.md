# eon 风格微服务脚手架（Spring Cloud + Boot + OAuth2 + RBAC）

项目结构参考 pig（已改名为 eon-*）：同时支持微服务与单体模式，优先完成 Nacos 接入。

核心版本（占位，需联网校验）：
- Spring Boot 3.5.5 / Spring Cloud 2025 / Spring Cloud Alibaba 2023
- Spring Authorization Server 1.5.2 / MyBatis Plus 3.5.12

## 模块结构

eon
- eon-boot — 单体模式启动器 [9999]
- eon-auth — 授权服务提供 [3000]
- eon-common — 系统公共模块（聚合）
  - eon-common-bom — 全局依赖管理控制（BOM）
  - eon-common-core — 公共工具类核心包
  - eon-common-datasource — 动态数据源包（占位）
  - eon-common-log — 日志服务（占位）
  - eon-common-oss — 文件上传工具类（占位）
  - eon-common-mybatis — mybatis 扩展封装
  - eon-common-seata — 分布式事务（占位）
  - eon-common-websocket — websocket 封装（占位）
  - eon-common-security — 安全工具类（基础安全依赖）
  - eon-common-swagger — 接口文档（占位）
  - eon-common-feign — feign 扩展封装
  - eon-common-xss — xss 安全封装（占位）
- eon-register — Nacos Server [8848]（docker-compose）
- eon-gateway — Spring Cloud Gateway 网关 [9999]
- eon-upms — 通用用户权限管理模块（聚合）
  - eon-upms-api — 公共 API 模块
  - eon-upms-biz — 业务处理模块 [4000]
- eon-visual — 辅助可视化（聚合）
  - eon-monitor — 服务监控 [5001]
  - eon-codegen — 图形化代码生成 [5002]
  - eon-quartz — 定时任务管理台 [5007]

## 构建与运行

1) 构建：`mvn -q -DskipTests package`

2) 启动依赖（推荐）：`docker compose -f infra/local/docker-compose.yml up -d`
   - 详情见 `infra/local/README.md`，默认提供 MySQL、Redis、Nacos、Zipkin。

3) 启动注册中心（若需单独部署）：`docker compose -f eon-register/docker-compose.yml up -d`

4) 启动微服务（示例）
- `java -jar eon-gateway/target/*.jar`
- `java -jar eon-auth/target/*.jar`
- `java -jar eon-upms/eon-upms-biz/target/*.jar`

5) 启动单体（与网关端口冲突，二选一）：
- `java -jar eon-boot/target/*.jar`

6) 访问与验证
- 网关发现路由：`http://localhost:9999/eon-upms-biz/actuator/health`
- UPMS 业务：`http://localhost:4000/actuator/health`

环境变量：`NACOS_SERVER_ADDR`（默认 `127.0.0.1:8848`），`NACOS_USERNAME`/`NACOS_PASSWORD`

版本兼容性提示：按需求占位，首次落地请联网校验官方兼容矩阵；如不兼容，优先正确性直接替换。
