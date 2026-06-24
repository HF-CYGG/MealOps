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

cleanup() {
  if [ "$KEEP_RUNNING" != "1" ]; then
    docker compose --env-file "$ENV_FILE" down
  fi
}
trap cleanup EXIT

FRONTEND_PORT="$(read_env_value FRONTEND_PORT 8088)"
BACKEND_PORT="$(read_env_value BACKEND_PORT 8080)"

if ! docker compose --env-file "$ENV_FILE" config; then
  echo "docker compose config failed" >&2
  exit 1
fi

if ! docker compose --env-file "$ENV_FILE" up -d --build; then
  echo "docker compose up failed" >&2
  exit 1
fi

if ! wait_http_ok "http://127.0.0.1:$BACKEND_PORT/health" "$TIMEOUT_SECONDS" ||
   ! wait_http_ok "http://127.0.0.1:$FRONTEND_PORT/client/home" "$TIMEOUT_SECONDS" ||
   ! wait_http_ok "http://127.0.0.1:$FRONTEND_PORT/login" "$TIMEOUT_SECONDS"; then
  echo "Recent backend logs:" >&2
  docker compose --env-file "$ENV_FILE" logs --tail=120 backend >&2 || true
  echo "Recent frontend logs:" >&2
  docker compose --env-file "$ENV_FILE" logs --tail=80 frontend >&2 || true
  exit 1
fi

docker compose --env-file "$ENV_FILE" ps
echo "MealOps Docker smoke test passed."
