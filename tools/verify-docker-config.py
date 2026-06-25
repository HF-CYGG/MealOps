from pathlib import Path
import re
import shutil
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]


def read(relative_path: str) -> str:
    return (ROOT / relative_path).read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def check_file_presence() -> None:
    required_files = [
        "Dockerfile",
        "docker-compose.yml",
        ".env.example",
        ".dockerignore",
        "frontend/Dockerfile",
        "frontend/.dockerignore",
        "frontend/nginx.conf",
        "frontend/src/utils/request.js",
        "docs/docker-deployment.md",
        ".gitignore",
        "tools/docker-smoke-test.ps1",
        "tools/docker-smoke-test.sh",
        "sql/schema.sql",
        "sql/data.sql",
    ]
    for relative_path in required_files:
        require((ROOT / relative_path).is_file(), f"missing required file: {relative_path}")


def check_compose() -> None:
    compose = read("docker-compose.yml")
    for service in ("mysql", "redis", "backend", "frontend"):
        require(re.search(rf"^  {service}:\s*$", compose, re.MULTILINE), f"missing service: {service}")

    require("MYSQL_DATABASE: reggie" in compose, "mysql service must use fixed reggie database")
    require("jdbc:mysql://mysql:3306/reggie?" in compose, "backend must connect to mysql service reggie database")
    require(
        "./sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro" in compose,
        "schema.sql must run before seed data",
    )
    require(
        "./sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro" in compose,
        "data.sql must run after schema.sql",
    )
    for mount in (
        "${MEALOPS_MYSQL_DATA_DIR:-./docker-data/mysql}:/var/lib/mysql",
        "${MEALOPS_REDIS_DATA_DIR:-./docker-data/redis}:/data",
        "${MEALOPS_UPLOADS_DIR:-./docker-data/uploads}:/app/uploads",
        "${MEALOPS_LOGS_DIR:-./docker-data/logs}:/app/logs",
    ):
        require(mount in compose, f"compose must support configurable host bind mount: {mount}")
    require("condition: service_healthy" in compose, "dependent services must wait for health checks")
    require("location = /healthz" in read("frontend/nginx.conf"), "frontend Nginx must expose a health endpoint")
    require("wget -qO- http://127.0.0.1/healthz" in compose, "frontend service must define a container health check")


def check_env_template() -> None:
    env_example = read(".env.example")
    require("MYSQL_DATABASE" not in env_example, ".env.example must not expose a database name that SQL cannot honor")
    for key in (
        "FRONTEND_PORT=",
        "BACKEND_PORT=",
        "MYSQL_ROOT_PASSWORD=",
        "MYSQL_PASSWORD=",
        "REDIS_PASSWORD=",
        "MEALOPS_MYSQL_DATA_DIR=",
        "MEALOPS_REDIS_DATA_DIR=",
        "MEALOPS_UPLOADS_DIR=",
        "MEALOPS_LOGS_DIR=",
        "MEALOPS_DB_INIT_ENABLED=",
        "MEALOPS_DB_INIT_SEED_DATA_ENABLED=",
        "MEALOPS_ADMIN_BOOTSTRAP_ENABLED=",
        "MEALOPS_ADMIN_USERNAME=",
        "MEALOPS_ADMIN_PASSWORD=",
        "MEALOPS_ADMIN_NAME=",
        "MEALOPS_ADMIN_PHONE=",
        "MEALOPS_JWT_SECRET=",
    ):
        require(key in env_example, f"missing env template key: {key}")
    gitignore = read(".gitignore")
    require("docker-data/" in gitignore, "docker-data host mount directory must be ignored by git")


def check_dockerfiles_and_nginx() -> None:
    backend_dockerfile = read("Dockerfile")
    frontend_dockerfile = read("frontend/Dockerfile")
    nginx_conf = read("frontend/nginx.conf")
    frontend_request = read("frontend/src/utils/request.js")

    require(backend_dockerfile.count("\nFROM ") + backend_dockerfile.startswith("FROM ") == 3, "root Dockerfile must build frontend and backend into one runtime image")
    require("# syntax=docker/dockerfile" not in backend_dockerfile, "backend Dockerfile must not pull Docker Hub syntax images")
    require("--mount=type=cache" not in backend_dockerfile, "backend Dockerfile should not require BuildKit-only cache mounts")
    require(
        "public.ecr.aws/docker/library/node:22-alpine AS frontend-build" in backend_dockerfile,
        "root Dockerfile must build Vue frontend with Node 22",
    )
    require("COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static" in backend_dockerfile, "root Dockerfile must embed frontend dist into Spring Boot static resources")
    require("COPY sql ./sql" in backend_dockerfile, "root Dockerfile must include SQL scripts for application startup initialization")
    require(
        "public.ecr.aws/docker/library/maven:3.9.11-eclipse-temurin-17 AS build" in backend_dockerfile,
        "backend build image must avoid Docker Hub anonymous pull limits",
    )
    require(
        "public.ecr.aws/docker/library/eclipse-temurin:17-jre-alpine" in backend_dockerfile,
        "backend runtime must use Java 17 JRE without Docker Hub anonymous pulls",
    )
    require("HEALTHCHECK" in backend_dockerfile, "backend Dockerfile must define a health check")
    require("chown -R mealops:mealops /app/uploads /app/logs" in backend_dockerfile, "backend entrypoint must fix bind mount ownership")
    require("su mealops -s /bin/sh -c" in backend_dockerfile, "backend entrypoint must drop privileges after fixing mount ownership")

    require("# syntax=docker/dockerfile" not in frontend_dockerfile, "frontend Dockerfile must not pull Docker Hub syntax images")
    require(
        "public.ecr.aws/docker/library/node:22-alpine AS build" in frontend_dockerfile,
        "frontend Dockerfile must build with Node 22 without Docker Hub anonymous pulls",
    )
    require(
        "public.ecr.aws/docker/library/nginx:1.27-alpine" in frontend_dockerfile,
        "frontend runtime must use Nginx without Docker Hub anonymous pulls",
    )
    require("npm ci" in frontend_dockerfile, "frontend Dockerfile must use npm ci for reproducible installs")

    require("try_files $uri $uri/ /index.html;" in nginx_conf, "Nginx must support Vue history routes")
    require("proxy_pass http://backend:8080/;" in nginx_conf, "Nginx must proxy /api to backend service")
    require("client_max_body_size 20m;" in nginx_conf, "Nginx upload limit must match Spring multipart max request size")
    require(
        "baseURL: '/api'" in frontend_request or 'baseURL: "/api"' in frontend_request,
        "frontend must use same-origin /api so Nginx or the single-container API filter can route requests to backend",
    )
    require("ApiPrefixForwardFilter" in read("src/main/java/com/cjc/mealops/config/ApiPrefixForwardFilter.java"), "single-container image must support /api-prefixed frontend requests")
    require("SpaForwardController" in read("src/main/java/com/cjc/mealops/controller/SpaForwardController.java"), "single-container image must support Vue history route fallback")
    require("DatabaseSchemaInitializer" in read("src/main/java/com/cjc/mealops/config/DatabaseSchemaInitializer.java"), "single-container image must initialize an empty external MySQL database")


def check_readme_docker_integration() -> None:
    readme = read("README.md")
    require(
        "docker compose --env-file .env up -d --build" in readme,
        "README must document the one-command Docker Compose startup",
    )
    require("`${FRONTEND_PORT:-8088}`" in readme, "README must document default frontend host port")
    require("`/api/`" in readme, "README must document same-origin frontend API requests")
    require("单容器镜像" in readme, "README must document the ACR/1Panel single project container deployment")
    require("MEALOPS_ADMIN_USERNAME" in readme, "README must document configurable initial admin username")
    require("MEALOPS_ADMIN_PASSWORD" in readme, "README must document configurable initial admin password")
    require("MEALOPS_DB_INIT_ENABLED" in readme, "README must document application database initialization")
    require("MEALOPS_DB_INIT_SEED_DATA_ENABLED" in readme, "README must document optional seed-data initialization")
    require("`backend:8080`" in readme, "README must document Nginx proxy target backend service")
    require("`mysql:3306/reggie`" in readme, "README must document backend default MySQL target")
    require("KEEP_RUNNING=1 sh tools/docker-smoke-test.sh" in readme, "README must recommend self-check startup on Linux/macOS")
    require(".\\tools\\docker-smoke-test.ps1 -KeepRunning" in readme, "README must recommend self-check startup on Windows")
    for token in (
        "MEALOPS_MYSQL_DATA_DIR",
        "MEALOPS_REDIS_DATA_DIR",
        "MEALOPS_UPLOADS_DIR",
        "MEALOPS_LOGS_DIR",
        "docker-data/",
    ):
        require(token in readme, f"README must document Docker mount setting: {token}")


def check_sql_alignment() -> None:
    schema = read("sql/schema.sql")
    data = read("sql/data.sql")
    require("CREATE DATABASE IF NOT EXISTS reggie" in schema, "schema.sql must create reggie database")
    require("SET NAMES utf8mb4;" in schema, "schema.sql must force mysql client charset to utf8mb4")
    require("USE reggie;" in schema, "schema.sql must select reggie database")
    require("SET NAMES utf8mb4;" in data, "data.sql must force mysql client charset to utf8mb4")
    require("USE reggie;" in data, "data.sql must select reggie database")
    require("'男'" in data, "seed employee sex value must fit employee.sex varchar(2)")
    require("'女'" in data, "seed user/address sex values must fit sex varchar(2)")
    require("'鐢?" not in data and "'濂?" not in data, "seed sex values must not contain mojibake that exceeds varchar(2)")


def check_smoke_test_scripts() -> None:
    powershell_script = read("tools/docker-smoke-test.ps1")
    shell_script = read("tools/docker-smoke-test.sh")
    docs = read("docs/docker-deployment.md")

    for script_name, script in (
        ("tools/docker-smoke-test.ps1", powershell_script),
        ("tools/docker-smoke-test.sh", shell_script),
    ):
        require("docker compose --env-file" in script, f"{script_name} must run docker compose with .env")
        require("up -d --build" in script, f"{script_name} must build and start the stack")
        require("mysql redis" in script, f"{script_name} must start infrastructure services before app services")
        require("backend frontend" in script, f"{script_name} must start app services after dependencies are healthy")
        require("frontend" in script, f"{script_name} must include frontend in startup readiness checks")
        require("/health" in script, f"{script_name} must verify backend health endpoint")
        require("/client/home" in script, f"{script_name} must verify frontend user route")
        require("/login" in script, f"{script_name} must verify frontend admin route")
        require("mysql" in script and "redis" in script, f"{script_name} must include dependency logs on smoke-test failure")
        require("logs --tail=120" in script, f"{script_name} must print service logs on smoke-test failure")

    require("$LASTEXITCODE" in powershell_script, "PowerShell smoke test must fail on non-zero docker exit codes")
    require("Wait-ContainerHealthy" in powershell_script, "PowerShell smoke test must wait for dependency health explicitly")
    require('$smokePassed = $false' in powershell_script, "PowerShell smoke test must track success before cleanup")
    require(
        "leaving containers running for inspection" in powershell_script,
        "PowerShell smoke test must keep containers running when startup self-check fails",
    )
    require("set -eu" in shell_script, "shell smoke test must fail fast")
    require("wait_container_healthy" in shell_script, "shell smoke test must wait for dependency health explicitly")
    require('SMOKE_SUCCESS="0"' in shell_script, "shell smoke test must track success before cleanup")
    require(
        "leaving containers running for inspection" in shell_script,
        "shell smoke test must keep containers running when startup self-check fails",
    )
    require("tools/docker-smoke-test.ps1" in docs, "deployment doc must mention PowerShell smoke test")
    require("tools/docker-smoke-test.sh" in docs, "deployment doc must mention shell smoke test")
    require("KEEP_RUNNING=1 sh tools/docker-smoke-test.sh" in docs, "deployment doc must recommend self-check startup on Linux/macOS")
    require(".\\tools\\docker-smoke-test.ps1 -KeepRunning" in docs, "deployment doc must recommend self-check startup on Windows")
    for token in (
        "MEALOPS_MYSQL_DATA_DIR",
        "MEALOPS_REDIS_DATA_DIR",
        "MEALOPS_UPLOADS_DIR",
        "MEALOPS_LOGS_DIR",
        "MEALOPS_DB_INIT_ENABLED",
        "MEALOPS_DB_INIT_SEED_DATA_ENABLED",
        "backend:8080",
    ):
        require(token in docs, f"deployment doc must document Docker setting: {token}")


def maybe_run_docker_compose_config() -> None:
    if shutil.which("docker") is None:
        print("docker CLI not found; skipped docker compose config")
        return

    result = subprocess.run(
        ["docker", "compose", "--env-file", ".env.example", "config"],
        cwd=ROOT,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
    )
    print(result.stdout)
    require(result.returncode == 0, "docker compose config failed")


def main() -> int:
    checks = [
        check_file_presence,
        check_compose,
        check_env_template,
        check_dockerfiles_and_nginx,
        check_readme_docker_integration,
        check_sql_alignment,
        check_smoke_test_scripts,
        maybe_run_docker_compose_config,
    ]
    for check in checks:
        check()

    print("Docker deployment configuration checks passed")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"Docker deployment configuration check failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
