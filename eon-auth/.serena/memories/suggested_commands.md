# 常用命令
- **构建打包**：`mvn -q -DskipTests package`（在项目根目录执行）。
- **单元测试**：`mvn -q test`。
- **启动服务**：`java -jar target/eon-auth-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev`（或使用生成的 Jar 通配）。
- **数据库初始化**：启动时自动执行 `src/main/resources/sql/schema.sql` 与 `data.sql`；如需校验客户端配置可运行 `./verify_db_setup.sh`。
- **OAuth2 密码模式联调**：
  - 全量脚本：`./test-oauth2-password.sh`
  - 精简脚本：`./test_password_auth.sh`
- **健康检查**：`curl http://localhost:3000/actuator/health` 或 `curl http://localhost:3000/api/health`。