# MealOps Docker 部署说明

本文档用于通过 Docker Compose 部署 MealOps 的管理端、用户端、后端、MySQL 和 Redis。

## 前置要求

- Docker Engine / Docker Desktop
- Docker Compose v2
- 首次构建需要能访问 Maven、npm 和 Docker Hub 镜像源

## 服务结构

| 服务 | 说明 | 默认端口 |
| --- | --- | --- |
| `frontend` | Nginx 托管 Vue 构建产物，并把 `/api/*` 反向代理到后端 | `8088 -> 80` |
| `backend` | Spring Boot API 服务 | `8080 -> 8080` |
| `mysql` | MySQL 8.4，首次启动自动执行 `sql/schema.sql` 与 `sql/data.sql` | `3306 -> 3306` |
| `redis` | Redis 7，支持可选密码 | `6379 -> 6379` |

## 首次启动

1. 复制环境变量模板：

```bash
cp .env.example .env
```

2. 修改 `.env` 中的敏感配置：

```env
MYSQL_ROOT_PASSWORD=change-this-mysql-password
MYSQL_PASSWORD=change-this-mysql-password
MEALOPS_JWT_SECRET=change-this-long-random-secret
```

3. 构建并启动：

```bash
docker compose --env-file .env up -d --build
```

4. 查看服务状态：

```bash
docker compose ps
```

## 访问地址

- 用户端：`http://localhost:8088/client/home`
- 管理端：`http://localhost:8088/login`
- 后端健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

如需修改端口，调整 `.env` 中的 `FRONTEND_PORT`、`BACKEND_PORT`、`MYSQL_PORT` 和 `REDIS_PORT`。

数据库名固定为 `reggie`，与 `sql/schema.sql` 和 `sql/data.sql` 保持一致。不要在 `.env` 中另行添加 `MYSQL_DATABASE`，否则会造成后端连接库名与初始化脚本库名不一致。

## 初始账号

由 `sql/data.sql` 写入：

- 管理员账号：`admin`
- 管理员密码：`admin1330`
- 用户端演示手机号：`13900000000`

## 数据与文件持久化

Compose 使用以下 named volume：

| Volume | 用途 |
| --- | --- |
| `mealops_mysql-data` | MySQL 数据 |
| `mealops_redis-data` | Redis AOF 数据 |
| `mealops_backend-uploads` | 后端上传文件 |
| `mealops_backend-logs` | 后端日志 |

后端容器内上传目录固定为 `/app/uploads`，通过 `MEALOPS_UPLOAD_DIR=/app/uploads` 注入。

## 常用运维命令

本地静态检查 Docker 部署配置：

```bash
python tools/verify-docker-config.py
```

如果当前机器安装了 Docker CLI，该脚本会额外执行 `docker compose --env-file .env.example config`；否则只执行不依赖 Docker 的文件一致性检查。

在 Docker 环境中执行完整冒烟验证。该脚本会执行 `docker compose --env-file .env up -d --build`，等待后端 `/health`、前端 `/client/home` 和 `/login` 可访问，默认验证完成后自动 `docker compose down`。

脚本路径：`tools/docker-smoke-test.ps1`、`tools/docker-smoke-test.sh`。

Windows PowerShell：

```powershell
.\tools\docker-smoke-test.ps1
```

Linux / macOS：

```bash
sh tools/docker-smoke-test.sh
```

如需验证后保留容器：

```powershell
.\tools\docker-smoke-test.ps1 -KeepRunning
```

```bash
KEEP_RUNNING=1 sh tools/docker-smoke-test.sh
```

查看日志：

```bash
docker compose logs -f backend
docker compose logs -f frontend
```

重新构建前后端：

```bash
docker compose build backend frontend
docker compose up -d backend frontend
```

停止服务但保留数据：

```bash
docker compose down
```

清空数据库、Redis、上传文件和日志后重新初始化：

```bash
docker compose down -v
docker compose --env-file .env up -d --build
```

## 数据库初始化顺序

`docker-compose.yml` 将脚本显式挂载为：

- `sql/schema.sql` -> `/docker-entrypoint-initdb.d/01-schema.sql`
- `sql/data.sql` -> `/docker-entrypoint-initdb.d/02-data.sql`

这样可以避免 Docker MySQL 按文件名排序时先执行 `data.sql` 的问题。

## 故障排查

### 前端能打开但接口失败

检查后端是否健康：

```bash
docker compose ps backend
docker compose logs backend
```

前端生产环境通过 Nginx 把 `/api/*` 转发到 `backend:8080`，不依赖 Vite dev proxy。

### 后端无法连接 MySQL

检查 `.env` 中：

```env
MYSQL_ROOT_PASSWORD=...
MYSQL_PASSWORD=...
```

默认后端使用 `root` 账号连接容器内 `mysql:3306/reggie`。

### Redis 设置了密码后健康检查失败

确认 `.env` 中 `REDIS_PASSWORD` 没有多余空格。为空时 Redis 不启用密码；非空时后端和 Redis healthcheck 都会使用该密码。

### 修改 SQL 后没有重新初始化

MySQL 初始化脚本只在 `mysql-data` volume 为空时执行。需要重新执行初始化时运行：

```bash
docker compose down -v
docker compose --env-file .env up -d --build
```
