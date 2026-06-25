# MealOps Docker 部署说明

## ACR / 1Panel 单项目容器

根目录 `Dockerfile` 会构建一个前后端一体镜像。构建过程先打包 Vue 前端，再把 `frontend/dist` 写入 Spring Boot 静态资源，运行时只启动一个 Java 容器即可同时提供前端页面和后端 API。

1Panel 手动创建项目容器时建议使用：

| 配置项 | 推荐值 |
| --- | --- |
| 镜像 | `crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops:latest` |
| 端口 | 宿主 `8088` -> 容器 `8080` |
| 上传挂载 | `/opt/MealOps/app/uploads` -> `/app/uploads`，读写 |
| 日志挂载 | `/opt/MealOps/app/logs` -> `/app/logs`，读写 |
| 管理端 | `http://服务器IP:8088/login` |
| 用户端 | `http://服务器IP:8088/client/home` |
| 健康检查 | `http://服务器IP:8088/health` |

项目容器仍需连接 MySQL 和 Redis。请将项目容器、MySQL、Redis 放在同一个 Docker 网络，并确保项目容器内能解析 `mysql` 和 `redis`；如果 1Panel 中实际容器名不同，请给 MySQL/Redis 添加网络别名，或调整 `MYSQL_URL`、`REDIS_HOST`。

```env
MYSQL_URL=jdbc:mysql://mysql:3306/reggie?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=mealops
MYSQL_PASSWORD=change-this-mysql-password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
MEALOPS_UPLOAD_DIR=/app/uploads
MEALOPS_JWT_SECRET=change-this-long-random-secret
MEALOPS_JWT_TTL_HOURS=2
LOG_PATH=/app/logs
TZ=Asia/Shanghai
```

一体镜像内部支持两类路由兼容：

- 前端请求 `/api/*` 会转发到后端原始接口，例如 `/api/employee/login` -> `/employee/login`。
- Vue history 路由会回退到 `index.html`，例如 `/login`、`/client/home` 可直接访问。

## Docker Compose 多容器部署

本文档用于通过 Docker Compose 部署 MealOps 的管理端、用户端、后端、MySQL 和 Redis。默认部署后，前端通过 Nginx 把同源 `/api/` 请求转发到后端容器 `backend:8080`，不需要额外填写前端 API 地址。

## 前置要求

- Docker Engine / Docker Desktop
- Docker Compose v2
- 首次构建需要能访问 Maven、npm 和容器镜像源

## 服务结构

| 服务 | 说明 | 默认端口 |
| --- | --- | --- |
| `frontend` | Nginx 托管 Vue 构建产物，并把 `/api/*` 反向代理到后端 | `8088 -> 80` |
| `backend` | Spring Boot API 服务 | `8080 -> 8080` |
| `mysql` | MySQL 8.4，首次启动自动执行 `sql/schema.sql` 与 `sql/data.sql` | `3306 -> 3306` |
| `redis` | Redis 7，支持可选密码 | `6379 -> 6379` |

## 首次启动

### Linux / macOS

```bash
cp .env.example .env
KEEP_RUNNING=1 sh tools/docker-smoke-test.sh
```

### Windows PowerShell

```powershell
Copy-Item .env.example .env
.\tools\docker-smoke-test.ps1 -KeepRunning
```

推荐使用上面的启动自检脚本作为默认启动方式。脚本会检查 Compose 配置，先启动 MySQL 和 Redis 并等待 `healthy`，再启动后端和前端并等待容器健康，最后检查后端 `/health`、前端 `/client/home` 和 `/login`。如果任一阶段失败，脚本会返回非零退出码、打印近期日志，并保留已启动容器，不会自动关闭现场。

如只需检查 Compose 配置：

```bash
docker compose --env-file .env config
```

## 必填配置建议

首次复制 `.env` 后，请至少修改或确认以下配置再用于长期环境：

```env
MEALOPS_MYSQL_DATA_DIR=/data/mealops/mysql
MEALOPS_REDIS_DATA_DIR=/data/mealops/redis
MEALOPS_UPLOADS_DIR=/data/mealops/uploads
MEALOPS_LOGS_DIR=/data/mealops/logs

MYSQL_ROOT_PASSWORD=change-this-mysql-password
MYSQL_PASSWORD=change-this-mysql-password
MEALOPS_JWT_SECRET=change-this-long-random-secret
```

Windows 路径请使用正斜杠，例如 `D:/mealops/mysql`。如果保留 `.env.example` 默认值，Docker Compose 会自动在项目目录下创建 `docker-data/`。

## 查看服务状态

```bash
docker compose --env-file .env ps
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

Compose 使用宿主机 bind mount。目录不存在时会自动创建。

| `.env` 变量 | 默认宿主目录 | 容器路径 | 用途 |
| --- | --- | --- | --- |
| `MEALOPS_MYSQL_DATA_DIR` | `./docker-data/mysql` | `/var/lib/mysql` | MySQL 数据 |
| `MEALOPS_REDIS_DATA_DIR` | `./docker-data/redis` | `/data` | Redis AOF 数据 |
| `MEALOPS_UPLOADS_DIR` | `./docker-data/uploads` | `/app/uploads` | 后端上传文件 |
| `MEALOPS_LOGS_DIR` | `./docker-data/logs` | `/app/logs` | 后端日志 |
| 固定挂载 | `./sql/schema.sql` | `/docker-entrypoint-initdb.d/01-schema.sql:ro` | 建表脚本 |
| 固定挂载 | `./sql/data.sql` | `/docker-entrypoint-initdb.d/02-data.sql:ro` | 演示数据脚本 |

后端容器内上传目录固定为 `/app/uploads`，通过 `MEALOPS_UPLOAD_DIR=/app/uploads` 注入；日志目录固定为 `/app/logs`，通过 `LOG_PATH=/app/logs` 注入。

## 图形化面板手动创建

优先使用 Docker Compose。若服务器面板必须手动创建容器，请使用同一个 bridge 网络，并把容器名或网络别名设置为 `mysql`、`redis`、`backend`、`frontend`。

| 容器 | 端口 | 挂载 | 必填环境变量 |
| --- | --- | --- | --- |
| MySQL | `3306:3306` | `${MEALOPS_MYSQL_DATA_DIR}:/var/lib/mysql`；`./sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro`；`./sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro` | `MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE=reggie`、`TZ=Asia/Shanghai` |
| Redis | `6379:6379` | `${MEALOPS_REDIS_DATA_DIR}:/data` | `REDIS_PASSWORD` 可留空 |
| 后端 | `8080:8080` | `${MEALOPS_UPLOADS_DIR}:/app/uploads`；`${MEALOPS_LOGS_DIR}:/app/logs` | `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_DATABASE`、`REDIS_PASSWORD`、`MEALOPS_UPLOAD_DIR`、`MEALOPS_JWT_SECRET`、`LOG_PATH`、`TZ` |
| 前端 | `8088:80` | 无需额外挂载 | 与后端在同一网络，Nginx 默认代理 `backend:8080` |

后端环境变量参考：

```env
MYSQL_URL=jdbc:mysql://mysql:3306/reggie?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=root
MYSQL_PASSWORD=change-this-mysql-password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
MEALOPS_UPLOAD_DIR=/app/uploads
MEALOPS_JWT_SECRET=change-this-long-random-secret
MEALOPS_JWT_TTL_HOURS=2
LOG_PATH=/app/logs
TZ=Asia/Shanghai
```

## 常用运维命令

本地静态检查 Docker 部署配置：

```bash
python tools/verify-docker-config.py
```

如果当前机器安装了 Docker CLI，该脚本会额外执行 `docker compose --env-file .env.example config`；否则只执行不依赖 Docker 的文件一致性检查。

在 Docker 环境中执行完整冒烟验证。该脚本会执行 `docker compose --env-file .env up -d --build`，等待 MySQL、Redis、后端、前端容器健康，并等待后端 `/health`、前端 `/client/home` 和 `/login` 可访问。

默认情况下，验证成功后脚本会停止容器但保留挂载目录数据；验证失败时会报错并保留已启动容器现场，不自动关闭，方便查看日志和容器状态。

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
docker compose --env-file .env logs -f backend
docker compose --env-file .env logs -f frontend
```

重新构建前后端：

```bash
docker compose --env-file .env build backend frontend
docker compose --env-file .env up -d backend frontend
```

停止服务但保留数据：

```bash
docker compose --env-file .env down
```

清空数据库、Redis、上传文件和日志后重新初始化：

```bash
docker compose --env-file .env down
```

然后删除 `.env` 中配置的四个宿主机目录：

- `MEALOPS_MYSQL_DATA_DIR`
- `MEALOPS_REDIS_DATA_DIR`
- `MEALOPS_UPLOADS_DIR`
- `MEALOPS_LOGS_DIR`

最后重新启动：

```bash
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

### 本地开发前端接口失败

如果使用的是 `npm run dev` 启动的 Vite 前端，请先确认本地后端 `http://localhost:8080` 已启动。开发环境的 `/api` 请求不会走 Docker 中的 `backend` 服务名。

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

MySQL 初始化脚本只在 `MEALOPS_MYSQL_DATA_DIR` 目录为空时执行。需要重新执行初始化时，先停止容器，再清空该宿主机目录，然后重新启动。

```bash
docker compose --env-file .env down
docker compose --env-file .env up -d --build
```
