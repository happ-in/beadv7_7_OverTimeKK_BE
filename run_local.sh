#!/bin/bash
# 로컬 개발용 인프라 실행 스크립트. compose 파일이 local/에 있어서 매번 -f 경로 안 쳐도 되게 감싼 것.
# 사용법:
#   ./run_local.sh up              MySQL/Redis 기동 (기본)
#   ./run_local.sh down            MySQL/Redis 종료
#   ./run_local.sh observability   Prometheus/Loki/Tempo/Grafana/cAdvisor/Promtail 기동
#   ./run_local.sh observability-down
set -e
cd "$(dirname "$0")"

# 프로젝트 이름을 repo root 기준(예전 compose.yaml이 root에 있을 때 쓰던 이름)으로 고정.
# 안 고정하면 compose 파일 위치(local/) 기준으로 이름이 정해져서, 기존 볼륨(디비 데이터)과
# 다른 새 볼륨이 생겨버림.
PROJECT_NAME="beadv7_7_overtimekk_be"

case "$1" in
  observability)
    docker compose -p "$PROJECT_NAME" -f local/observability-compose.yaml up -d
    ;;
  observability-down)
    docker compose -p "$PROJECT_NAME" -f local/observability-compose.yaml down
    ;;
  down)
    docker compose -p "$PROJECT_NAME" -f local/compose.yaml down
    ;;
  up|"")
    docker compose -p "$PROJECT_NAME" -f local/compose.yaml up -d
    ;;
  *)
    echo "usage: $0 [up|down|observability|observability-down]" >&2
    exit 1
    ;;
esac
