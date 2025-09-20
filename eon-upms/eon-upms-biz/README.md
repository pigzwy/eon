# eon-upms-biz 用户权限管理服务

> 角色：提供用户、角色、权限等主数据的微服务实现，默认端口 `3200`。

## 1. 当前状态
- 启动类 `EonUpmsBizApplication` 已就绪，可直接运行；
- 其余业务逻辑待补充，可参考 `eon-user` 模块的实现思路；
- 已集成 Nacos 注册/配置能力，支持集中管理配置。

## 2. 推荐落地项
1. **领域模型**：设计 `User`、`Role`、`Permission` 等实体与关系表；
2. **接口设计**：提供基础 CRUD、批量授权、策略版本等 REST API；
3. **安全框架**：引入 `eon-common-security`，利用 `@CurrentUser` 注解获得网关透传用户；
4. **缓存策略**：针对权限树、菜单等热点数据使用 Redis/Caffeine 缓存；
5. **数据脚本**：提供 `schema.sql` + `data.sql`，方便新环境初始化；
6. **单元/集成测试**：覆盖核心用例，保障权限模型的正确性。

## 3. 启动示例
```bash
mvn -pl eon-upms/eon-upms-biz -am package -DskipTests
java -jar target/eon-upms-biz-*.jar --spring.profiles.active=dev
```

## 4. 与其他模块的协作
- **eon-auth**：提供认证服务所需的用户/角色数据；
- **eon-gateway**：基于路由 metadata 控制访问；
- **eon-common-security**：解析当前用户信息，用于审计与数据权限。
