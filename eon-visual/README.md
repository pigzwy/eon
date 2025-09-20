# eon-visual 辅助可视化套件

> 角色：提供监控、代码生成、任务调度等运维/开发辅助工具的聚合工程。

## 1. 模块概览
| 模块 | 默认端口 | 主要功能 |
| ---- | -------- | -------- |
| `eon-monitor` | 5001 | Spring Boot Admin / 监控告警占位服务，用于统一查看微服务状态 |
| `eon-codegen` | 5002 | 图形化代码生成器（CRUD 模板、前端页面） |
| `eon-quartz` | 5007 | 定时任务管理后台，提供任务编排与运行状态监控 |

> 当前代码为骨架，主要提供启动类与基础配置；实际业务功能可根据项目需要逐步完善。

## 2. 构建方式
```bash
# 全部构建
mvn -pl eon-visual -am package -DskipTests

# 单独启动某个服务
mvn -pl eon-visual/eon-monitor -am spring-boot:run
```

## 3. 推荐落地项
1. **eon-monitor**：接入 Spring Boot Admin 或 SkyWalking UI，开启告警规则；
2. **eon-codegen**：整合 MyBatis-Plus 代码生成器，提供数据库元数据查询、模板定制功能；
3. **eon-quartz**：引入 Quartz/SchedulerX，实现任务 CRUD、运行日志、失败告警；
4. **统一登录**：所有可视化模块统一接入 `eon-auth`，通过网关鉴权；
5. **部署建议**：使用 docker-compose / Helm Chart 独立部署，并限制访问源 IP。

## 4. 后续文档
随着功能完善，请在各子模块下补充 README，记录部署、权限、常见问题等信息。
