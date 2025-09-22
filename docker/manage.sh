#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INFRA_COMPOSE="${SCRIPT_DIR}/docker-compose.infra.yml"
APPS_COMPOSE="${SCRIPT_DIR}/docker-compose.apps.yml"
INFRA_NETWORK="eon_infra_net"

usage() {
  cat <<'USAGE'
用法：
  ./docker/manage.sh infra start [额外参数]   # 启动基础设施
  ./docker/manage.sh infra stop [额外参数]    # 停止基础设施
  ./docker/manage.sh infra status             # 查看基础设施容器状态
  ./docker/manage.sh infra logs [服务]        # 查看基础设施日志
  ./docker/manage.sh apps build [额外参数]    # 构建业务镜像
  ./docker/manage.sh apps start [额外参数]    # 启动业务服务
  ./docker/manage.sh apps stop [额外参数]     # 停止业务服务
  ./docker/manage.sh apps status              # 查看业务容器状态
  ./docker/manage.sh apps logs [服务]         # 查看业务服务日志
  ./docker/manage.sh all start                # 依次启动基础设施与业务服务
  ./docker/manage.sh all stop                 # 依次停止业务服务与基础设施
  ./docker/manage.sh help                     # 查看帮助
USAGE
}

require_docker() {
  if ! command -v docker &>/dev/null; then
    echo "未检测到 docker 命令，请先安装 Docker。" >&2
    exit 1
  fi
}

ensure_infra_network() {
  if ! docker network inspect "${INFRA_NETWORK}" >/dev/null 2>&1; then
    echo "检测到网络 ${INFRA_NETWORK} 不存在，请先执行 './docker/manage.sh infra start' 启动基础设施。" >&2
    exit 1
  fi
}

run_compose() {
  local file="$1"
  shift
  docker compose -f "${file}" "$@"
}

require_docker

command=${1:-help}
shift || true

case "${command}" in
  infra)
    subcommand=${1:-}
    shift || true
    case "${subcommand}" in
      start)
        run_compose "${INFRA_COMPOSE}" up -d "$@"
        ;;
      stop)
        run_compose "${INFRA_COMPOSE}" down "$@"
        ;;
      status)
        run_compose "${INFRA_COMPOSE}" ps "$@"
        ;;
      logs)
        run_compose "${INFRA_COMPOSE}" logs "$@"
        ;;
      *)
        usage
        exit 1
        ;;
    esac
    ;;
  apps)
    subcommand=${1:-}
    shift || true
    case "${subcommand}" in
      build)
        run_compose "${APPS_COMPOSE}" build "$@"
        ;;
      start)
        ensure_infra_network
        run_compose "${APPS_COMPOSE}" up -d "$@"
        ;;
      stop)
        run_compose "${APPS_COMPOSE}" down "$@"
        ;;
      status)
        run_compose "${APPS_COMPOSE}" ps "$@"
        ;;
      logs)
        run_compose "${APPS_COMPOSE}" logs "$@"
        ;;
      *)
        usage
        exit 1
        ;;
    esac
    ;;
  all)
    subcommand=${1:-}
    shift || true
    case "${subcommand}" in
      start)
        run_compose "${INFRA_COMPOSE}" up -d "$@"
        ensure_infra_network
        run_compose "${APPS_COMPOSE}" up -d "$@"
        ;;
      stop)
        run_compose "${APPS_COMPOSE}" down "$@"
        run_compose "${INFRA_COMPOSE}" down "$@"
        ;;
      *)
        usage
        exit 1
        ;;
    esac
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    usage
    exit 1
    ;;
 esac
