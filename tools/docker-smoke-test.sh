#!/usr/bin/env sh
set -eu

ENV_FILE="${ENV_FILE:-.env}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-180}"
KEEP_RUNNING="${KEEP_RUNNING:-0}"

cd "$(dirname "$0")/.."

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker CLI not found. Install Docker Desktop or run this script on a Docker host." >&2
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  cp .env.example "$ENV_FILE"
  echo "Created $ENV_FILE from .env.example. Review secrets before production use."
fi

read_env_value() {
  name="$1"
  default="$2"
  value="$(grep -E "^[[:space:]]*$name[[:space:]]*=" "$ENV_FILE" 2>/dev/null | head -n 1 | sed -E "s/^[[:space:]]*$name[[:space:]]*=[[:space:]]*//" | sed -E "s/^['\"]//;s/['\"]$//")"
  if [ -z "$value" ]; then
    printf '%s' "$default"
  else
    printf '%s' "$value"
  fi
}

wait_http_ok() {
  url="$1"
  timeout="$2"
  start="$(date +%s)"
  while :; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    now="$(date +%s)"
    if [ $((now - start)) -ge "$timeout" ]; then
      echo "Timed out waiting for $url" >&2
      return 1
    fi
    sleep 3
  done
}

compose() {
  docker compose --env-file "$ENV_FILE" "$@"
}

dump_logs() {
  echo "Docker Compose service status:" >&2
  compose ps >&2 || true
  for service in mysql redis backend frontend; do
    echo "Recent $service logs:" >&2
    compose logs --tail=120 "$service" >&2 || true
  done
}

wait_container_healthy() {
  service="$1"
  timeout="$2"
  start="$(date +%s)"
  while :; do
    container_id="$(compose ps -q "$service" 2>/dev/null || true)"
    if [ -z "$container_id" ]; then
      echo "Container for $service was not created" >&2
      dump_logs
      return 1
    fi

    state="$(docker inspect -f '{{.State.Status}}' "$container_id" 2>/dev/null || true)"
    health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' "$container_id" 2>/dev/null || true)"
    if [ "$health" = "healthy" ]; then
      return 0
    fi
    if [ "$state" = "exited" ] || [ "$state" = "dead" ]; then
      echo "$service exited before becoming healthy" >&2
      dump_logs
      return 1
    fi

    now="$(date +%s)"
    if [ $((now - start)) -ge "$timeout" ]; then
      echo "Timed out waiting for $service to become healthy; state=$state health=$health" >&2
      dump_logs
      return 1
    fi
    sleep 3
  done
}

cleanup() {
  if [ "$KEEP_RUNNING" != "1" ]; then
    compose down
  fi
}
trap cleanup EXIT

FRONTEND_PORT="$(read_env_value FRONTEND_PORT 8088)"
BACKEND_PORT="$(read_env_value BACKEND_PORT 8080)"

if ! compose config; then
  echo "docker compose config failed" >&2
  exit 1
fi

if ! compose up -d --build mysql redis; then
  echo "docker compose infra startup failed" >&2
  dump_logs
  exit 1
fi

wait_container_healthy mysql "$TIMEOUT_SECONDS"
wait_container_healthy redis "$TIMEOUT_SECONDS"

if ! compose up -d --build backend frontend; then
  echo "docker compose app startup failed" >&2
  dump_logs
  exit 1
fi

if ! wait_http_ok "http://127.0.0.1:$BACKEND_PORT/health" "$TIMEOUT_SECONDS" ||
   ! wait_http_ok "http://127.0.0.1:$FRONTEND_PORT/client/home" "$TIMEOUT_SECONDS" ||
   ! wait_http_ok "http://127.0.0.1:$FRONTEND_PORT/login" "$TIMEOUT_SECONDS"; then
  dump_logs
  exit 1
fi

compose ps
echo "MealOps Docker smoke test passed."
