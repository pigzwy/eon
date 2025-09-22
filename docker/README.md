# eon 微服务容器化部署指引

本文档说明如何在本地或测试环境中使用 Docker Compose 将 `eon-auth`、`eon-gateway`、`eon-user` 三个应用服务与基础设施分离部署。部署分为两大阶段：先启动基础设施，再启动业务应用。

## 1. 前置条件
- 已安装 Docker 20.10+ 与 Docker Compose V2（`docker compose` 命令）。
- 已克隆本仓库，并在仓库根目录执行以下命令。

若是 Mac/Windows Docker Desktop，默认已满足；Linux 环境可参考官方安装指引。

## 2. 启动基础设施
基础设施编排文件位于 `docker/docker-compose.infra.yml`，包含 MySQL、Redis、Nacos，并统一挂载网络 `eon_infra_net`。推荐借助统一脚本简化操作。

- 脚本方式：
  ```bash
  ./docker/manage.sh infra start
  ```
- 直接命令：
  ```bash
  docker compose -f docker/docker-compose.infra.yml up -d
  ```

等待容器状态为 `healthy` 后再进行下一步，可通过以下方式检查：

```bash
docker compose -f docker/docker-compose.infra.yml ps
# 或
./docker/manage.sh infra status
```

如需查看共享网络是否创建成功：

```bash
docker network inspect eon_infra_net
```

停止或清理基础设施：

- 脚本方式：`./docker/manage.sh infra stop`
- 直接命令：`docker compose -f docker/docker-compose.infra.yml down`

> 注意：若要一并清除持久化数据，需要为 `stop` 命令追加 `-v` 参数。

## 3. 构建并启动业务服务
业务侧编排文件位于 `docker/docker-compose.apps.yml`，依赖外部网络 `eon_infra_net`。请先确保基础设施已启动。

1. 构建应用镜像（首次执行或代码发生变化时需要运行）：
   - 脚本方式：
     ```bash
     ./docker/manage.sh apps build
     ```
   - 直接命令：
     ```bash
     docker compose -f docker/docker-compose.apps.yml build
     ```

2. 拉起 `eon-auth`、`eon-user`、`eon-gateway` 三个容器：
   - 脚本方式：
     ```bash
     ./docker/manage.sh apps start
     ```
   - 直接命令：
     ```bash
     docker compose -f docker/docker-compose.apps.yml up -d
     ```

3. 查看业务容器状态：
   ```bash
   ./docker/manage.sh apps status
   # 或
   docker compose -f docker/docker-compose.apps.yml ps
   ```

4. 跟踪某个服务的日志（示例跟踪网关）：
   ```bash
   ./docker/manage.sh apps logs eon-gateway
   # 或
   docker compose -f docker/docker-compose.apps.yml logs -f eon-gateway
   ```

停止业务服务：
- 脚本方式：`./docker/manage.sh apps stop`
- 直接命令：`docker compose -f docker/docker-compose.apps.yml down`

如需一键启动/停止全部服务，可执行：

```bash
./docker/manage.sh all start
./docker/manage.sh all stop
```

## 4. 服务验证
- 鉴权服务健康检查：<http://localhost:3000/actuator/health>
- 用户服务健康检查：<http://localhost:3001/actuator/health>
- 网关健康检查：<http://localhost:3100/actuator/health>

如需验证链路，可通过网关访问用户接口：

```bash
curl -i http://localhost:3100/api/users/health
```

> 构建镜像时建议启用 Docker BuildKit（Docker Desktop 默认开启）。若本地禁用了 BuildKit，可手动添加 `DOCKER_BUILDKIT=1` 前缀以便复用 Maven 依赖缓存。

## 5. 配置覆盖与扩展
- Compose 中的环境变量可在命令行通过 `-e` 参数覆盖，或在 `deploy` 目录新增 `.env` 文件（参考 Compose 文档）。
- 若需自定义数据库账号、Redis 密码等，请同步修改 `docker/docker-compose.infra.yml` 与 `docker/docker-compose.apps.yml` 中的对应变量，并重启相关容器。
- 生产环境建议将镜像推送到镜像仓库，并使用独立的 Compose/Helm Charts，本文示例主要面向本地开发与集成演示场景。

## 6. 常见问题排查
1. **服务无法连接数据库**：确认基础设施已启动且 `eon_infra_net` 网络存在，可通过 `docker network ls` 或 `docker network inspect eon_infra_net` 检查；必要时重新执行基础设施 Compose。
2. **端口冲突**：默认暴露端口为 3000/3001/3100，可在 Compose 文件中按需修改 `ports` 映射。
3. **Nacos 未注册服务**：检查 `eon-auth`、`eon-user` 的日志，确认是否能够连接到 `http://nacos:8848`，必要时重启 `nacos` 容器或清理其数据卷。

---
通过以上步骤，即可完成基础设施与业务服务的分离部署，实现本地一键启动与验证。
