# 任务完成核对
- 关键接口或脚本需在本地对 `/oauth2/token`、`/actuator/health` 等端点跑通验证。
- 确认数据库（MySQL）中 `oauth2_registered_client` 与用户表数据与配置一致，必要时执行 `./verify_db_setup.sh`。
- 检查日志是否包含异常（`logs/` 或控制台输出）。
- 若修改配置或脚本，更新对应文档（如 `OAuth2-Testing-Guide.md`）。
- 完成后考虑执行 `mvn -q test` 以确保回归通过。