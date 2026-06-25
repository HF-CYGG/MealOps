# MealOps

MealOps 是一个基于 Spring Boot 3 与 Vue 3 的外卖点餐实训项目，提供管理端、用户端、后端接口、MySQL 初始化脚本、Redis 登录安全组件，以及 Docker Compose 一键部署方案。

## 项目组成

- 后端：Spring Boot 3 API，默认端口 `8080`
- 前端：Vue 3 + Vite 管理端与用户端，开发端口默认 `5173`
- 静态联调页：后端内置 `admin.html` 与 `user.html`，适合接口联调与验收演示
- Docker 部署：MySQL、Redis、后端、前端四个服务统一编排

## 技术栈

- Java 17
- Spring Boot 3.5.9
- MyBatis-Plus 3.5.16
- MySQL 8.x
- Redis 6+
- Vue 3
- Vite 8
- Element Plus
- Springdoc OpenAPI 2.8.17
- Docker Compose

## 本地配置

默认服务端口为 `8080`，默认数据库为 `reggie`。敏感配置通过环境变量覆盖，不建议写入仓库。

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_URL` | `jdbc:mysql://localhost:3306/reggie?...` | MySQL 连接地址 |
| `MYSQL_USERNAME` | `root` | MySQL 用户名 |
| `MYSQL_PASSWORD` | 空 | MySQL 密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_DATABASE` | `0` | Redis 库 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |
| `MEALOPS_UPLOAD_DIR` | `uploads` | 文件上传目录 |
| `MEALOPS_JWT_SECRET` | `mealops-local-secret-change-me` | 本地开发 JWT 密钥 |
| `MEALOPS_JWT_TTL_HOURS` | `2` | JWT 有效小时数 |

本地联调建议：

- 可在项目根目录新建 `.env.local` 保存本机专用账号密码，该文件已被 Git 忽略，不会同步到远程仓库。
- 根目录执行 `npm run dev` 或 `npm run dev:backend:local` 时，会自动读取 `.env.local` 并注入后端启动环境。
- 如果你在终端里手动设置了同名环境变量，终端中的值优先级更高，可临时覆盖 `.env.local`。

## 数据库初始化

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p reggie < sql/data.sql
```

## 启动方式速览

| 场景 | 适用对象 | 启动命令 | 主要访问地址 |
| --- | --- | --- | --- |
| 根目录联动启动 | 默认本地开发，前后端一起启动 | 先执行 `npm install`，再执行 `npm run dev` | `http://localhost:5173/login` |
| 后端本地启动 | 只调接口、Swagger、静态联调页 | 推荐执行 `npm run dev:backend:local` | `http://localhost:8080/health` |
| 前端本地开发 | 调试 Vue 页面与路由 | 进入 `frontend` 后，先执行 `npm install`，再执行 `npm run dev` | `http://localhost:5173/login` |
| Docker 一键部署 | 需要完整前后端 + MySQL + Redis | `docker compose --env-file .env up -d --build` | `http://localhost:8088/login` |

## 本地启动

### 默认联动启动

如果你希望默认一次命令同时启动前后端，请在项目根目录执行：

```bash
npm install
npm run dev
```

说明：

- 根目录 `npm run dev` 会同时执行后端 `mvn spring-boot:run` 和前端 `frontend` 目录下的 `npm run dev`。
- 根目录 `npm run dev` 会优先读取项目根目录下被 Git 忽略的 `.env.local`，适合保存本机专用数据库账号密码。
- 第一次使用时只需要在根目录执行一次 `npm install`，用于安装联动启动依赖 `concurrently`。
- 如果你不想每次手动输入数据库账号密码，建议直接维护 `.env.local`。
- 如果前端 `5173` 或后端 `8080` 被占用，启动日志会直接提示对应端口冲突。

### 1. 启动后端

如果你已经在根目录维护了 `.env.local`，推荐直接执行：

```powershell
npm run dev:backend:local
```

如果你暂时不想创建 `.env.local`，也可以先在当前终端注入环境变量后再启动：

```powershell
$env:MYSQL_USERNAME='root'
$env:MYSQL_PASSWORD='你的MySQL密码'
mvn spring-boot:run
```

如果数据库无密码，也可以直接执行：

```bash
mvn spring-boot:run
```

后端启动后可访问：

- 健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- 管理端静态联调页：`http://localhost:8080/static/admin.html`
- 用户端静态联调页：`http://localhost:8080/static/user.html`

说明：

- 静态联调页由后端直接托管，不依赖 Vite。
- 如果你只需要验接口或演示基础流程，只启动后端即可。
- 若需调试 Vue 前端页面，还需要单独启动前端开发服务器。

常见问题排查：

- 若提示 `Port 8080 was already in use`，请先释放 `8080` 端口，或修改后端端口并同步更新前端代理目标地址。
- 若提示数据库连接失败，请优先检查 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 是否与本机 MySQL 一致。
- 若前端页面能打开但接口全部失败，请先确认后端 `http://localhost:8080/health` 可访问。

### 2. 启动前端开发服务器

在新的终端进入 `frontend` 目录：

```bash
npm install
npm run dev
```

前端开发服务器默认访问地址：

- 管理端：`http://localhost:5173/login`
- 用户端：`http://localhost:5173/client/home`

说明：

- 前端通过 Vite 代理把 `/api` 请求转发到 `http://localhost:8080`。
- 启动前端前，请先确认后端已经运行，否则页面能打开但接口会报错。
- 终端会额外打印管理端与用户端入口地址，方便直接点击或复制。

## Docker 部署

项目已提供 Docker Compose 部署文件，包含 MySQL、Redis、Spring Boot 后端和 Nginx 前端。

推荐使用 `docker compose` 启动完整栈；如果在图形化容器面板中手动创建容器，请按本节的端口、网络、挂载和环境变量保持一致。

默认前后端联调行为：

- 执行 `docker compose --env-file .env up -d --build` 会同时启动 MySQL、Redis、后端和前端。
- 前端容器默认通过 Nginx 暴露在宿主 `${FRONTEND_PORT:-8088}`，访问 `http://localhost:8088/login` 或 `/client/home` 即可进入页面。
- 前端代码默认请求同源 `/api`，Nginx 会自动把 `/api/` 转发到后端容器 `backend:8080`；不需要在浏览器或前端容器里手动填写后端地址。
- 后端容器默认连接 MySQL 服务名 `mysql` 的 `3306` 端口，即 `mysql:3306/reggie`。

Linux / macOS：

```bash
cp .env.example .env
docker compose --env-file .env up -d --build
```

Windows PowerShell：

```powershell
Copy-Item .env.example .env
docker compose --env-file .env up -d --build
```

### Docker Compose 默认容器

| 服务 | 镜像/构建 | 容器名 | 宿主端口 -> 容器端口 | 说明 |
| --- | --- | --- | --- | --- |
| MySQL | `mysql:8.4` | `mealops-mysql` | `${MYSQL_PORT:-3306}` -> `3306` | 默认暴露宿主 `3306`，数据库名固定为 `reggie` |
| Redis | `redis:7-alpine` | `mealops-redis` | `${REDIS_PORT:-6379}` -> `6379` | 默认无密码，可通过 `REDIS_PASSWORD` 启用 |
| 后端 | 根目录 `Dockerfile` | `mealops-backend` | `${BACKEND_PORT:-8080}` -> `8080` | 容器内默认连接 `mysql:3306/reggie` |
| 前端 | `frontend/Dockerfile` | `mealops-frontend` | `${FRONTEND_PORT:-8088}` -> `80` | Nginx 托管 Vue，并将 `/api/` 转发到 `backend:8080` |

Compose 会创建默认 bridge 网络，后端通过服务名访问依赖：

- MySQL：`mysql:3306`
- Redis：`redis:6379`
- 后端：`backend:8080`

### 手动创建容器配置

如果不使用 `docker compose`，而是在容器面板中手动创建容器，建议使用同一个 bridge 网络，并设置容器名称或网络别名为 `mysql`、`redis`、`backend`、`frontend`，否则后端和前端内置的服务名访问会失败。

手动创建时的关键配置：

| 容器 | 端口 | 挂载 | 必填环境变量 |
| --- | --- | --- | --- |
| MySQL | `3306:3306` | 数据目录挂载到 `/var/lib/mysql`；首次初始化时将 `sql/schema.sql`、`sql/data.sql` 只读挂载到 `/docker-entrypoint-initdb.d/` | `MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE=reggie` |
| Redis | `6379:6379` | 数据目录挂载到 `/data` | 如需密码，设置 `REDIS_PASSWORD` |
| 后端 | `8080:8080` | 上传目录挂载到 `/app/uploads`；日志目录挂载到 `/app/logs` | `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`MEALOPS_JWT_SECRET` |
| 前端 | `8088:80` | 一般不需要额外挂载 | 需要与后端在同一网络，Nginx 默认代理 `backend:8080` |

后端手动容器推荐环境变量示例：

```env
MYSQL_URL=jdbc:mysql://mysql:3306/reggie?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=root
MYSQL_PASSWORD=mealops-dev-root
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
MEALOPS_UPLOAD_DIR=/app/uploads
MEALOPS_JWT_SECRET=change-this-secret-before-production
MEALOPS_JWT_TTL_HOURS=2
LOG_PATH=/app/logs
TZ=Asia/Shanghai
```

### `.env` 可配置变量

复制 `.env.example` 为 `.env` 后，可以按需修改以下变量：

| 变量 | 默认值 | 作用范围 | 说明 |
| --- | --- | --- | --- |
| `FRONTEND_PORT` | `8088` | 前端容器端口映射 | 宿主访问前端的端口，映射到容器 `80` |
| `BACKEND_PORT` | `8080` | 后端容器端口映射 | 宿主访问后端 API/健康检查的端口，映射到容器 `8080` |
| `MYSQL_PORT` | `3306` | MySQL 容器端口映射 | 宿主访问 MySQL 的端口，映射到容器 `3306`；容器内默认仍连接 `mysql:3306` |
| `REDIS_PORT` | `6379` | Redis 容器端口映射 | 宿主访问 Redis 的端口，映射到容器 `6379`；后端容器内默认连接 `redis:6379` |
| `MYSQL_ROOT_PASSWORD` | `mealops-dev-root` | MySQL 容器 | MySQL `root` 用户密码 |
| `MYSQL_USERNAME` | `root` | 后端容器 | 后端连接 MySQL 使用的用户名 |
| `MYSQL_PASSWORD` | `mealops-dev-root` | 后端容器 | 后端连接 MySQL 使用的密码；默认应与 `MYSQL_ROOT_PASSWORD` 一致 |
| `REDIS_DATABASE` | `0` | 后端容器 | 后端使用的 Redis database 编号 |
| `REDIS_PASSWORD` | 空 | Redis/后端容器 | Redis 密码；为空时 Redis 不启用密码，后端也以空密码连接 |
| `MEALOPS_JWT_SECRET` | `change-this-secret-before-production` | 后端容器 | JWT 签名密钥，生产环境必须修改 |
| `MEALOPS_JWT_TTL_HOURS` | `2` | 后端容器 | JWT 有效小时数 |

### 容器内固定配置

以下配置在 `docker-compose.yml` 或 Dockerfile 中固定，通常不需要在 `.env` 中修改：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_DATABASE` | `reggie` | MySQL 初始化数据库名 |
| `MYSQL_URL` | `jdbc:mysql://mysql:3306/reggie?...` | 后端容器连接 MySQL 的 JDBC 地址，默认连接 MySQL 容器 `3306` 端口 |
| `REDIS_HOST` | `redis` | 后端容器连接 Redis 的服务名 |
| 后端 `REDIS_PORT` | `6379` | 后端容器连接 Redis 的容器内端口 |
| `MEALOPS_UPLOAD_DIR` | `/app/uploads` | 后端上传文件目录 |
| `LOG_PATH` | `/app/logs` | 后端日志目录 |
| `TZ` | `Asia/Shanghai` | MySQL、后端容器时区 |

### 数据与日志挂载

| 卷/挂载 | 容器路径 | 用途 |
| --- | --- | --- |
| `mysql-data` | `/var/lib/mysql` | MySQL 数据持久化 |
| `redis-data` | `/data` | Redis AOF 数据持久化 |
| `backend-uploads` | `/app/uploads` | 后端上传文件持久化 |
| `backend-logs` | `/app/logs` | 后端日志持久化 |
| `./sql/schema.sql` | `/docker-entrypoint-initdb.d/01-schema.sql:ro` | MySQL 首次启动建表脚本 |
| `./sql/data.sql` | `/docker-entrypoint-initdb.d/02-data.sql:ro` | MySQL 首次启动演示数据脚本 |

默认访问地址：

- 用户端：`http://localhost:8088/client/home`
- 管理端：`http://localhost:8088/login`
- 后端健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

详细部署、重建、日志、冒烟验证和数据重置说明见 `docs/docker-deployment.md`。

## 初始账号与演示数据

初始化账号：

- 管理员账号：`admin`
- 管理员密码：`admin1330`
- 用户端演示手机号：`13900000000`
- 数据库中的管理员密码摘要：`849199be4c873a9ab895576681da12f3`

示例数据包含分类、菜品、口味、套餐、用户、地址、购物车、订单和操作日志。

## 静态联调页

`src/main/resources/static/admin.html` 和 `src/main/resources/static/user.html` 使用原生 JavaScript 调用后端接口，并把响应 JSON 直接输出到页面。页面会把登录 token 保存到 `localStorage`，请求时同时携带 `Authorization: Bearer <token>` 和 `token` 请求头，便于兼容不同拦截器实现。

## 前端开发说明

前端工程位于 `frontend/`，其启动、构建与访问入口说明见 `frontend/README.md`。

## GitHub Actions / ACR 自动构建

仓库提供 `.github/workflows/docker-validate.yml`，会在推送到 `main`、`codex/**` 或 `release-v*` 标签时运行后端测试、前端构建和 Docker 镜像构建验证。阿里云 ACR 镜像发布由 ACR 控制台构建规则自动完成：

```text
crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops
```

GitHub Actions 不登录、不推送 ACR，也不需要配置 `ALIYUN_ACR_USERNAME` 或 `ALIYUN_ACR_PASSWORD`。详细说明见 `docs/github-actions-acr.md`。

## 文档留痕

- `docs/api-checklist.md`：接口验收清单
- `docs/test-record-template.md`：测试记录模板
- `docs/report-screenshot-checklist.md`：实训报告截图清单
- `docs/docker-deployment.md`：Docker 部署与排障说明
- `docs/github-actions-acr.md`：GitHub Actions 与 ACR 自动构建说明

## 目录说明

```text
sql/
  schema.sql              MySQL 表结构与建库脚本
  data.sql                初始化数据
src/main/resources/
  application.yml         应用配置
  logback-spring.xml      日志配置
  static/
    admin.html            管理端原生 JS 联调页
    user.html             用户端原生 JS 联调页
frontend/
  README.md               前端本地开发说明
  src/                    Vue 前端源码
docs/
  api-checklist.md
  docker-deployment.md
  github-actions-acr.md
  test-record-template.md
  report-screenshot-checklist.md
```
