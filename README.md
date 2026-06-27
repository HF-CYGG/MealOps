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

### 单容器镜像（ACR / 1Panel 推荐）

阿里云 ACR 如果只构建根目录 `Dockerfile`，生成的 `mealops:latest` 是“前端 + 后端”一体镜像：Docker 构建阶段先执行 Vue 打包，再把 `frontend/dist` 放入 Spring Boot 静态资源目录，最终运行时只启动一个 Java 进程。

这种模式下不需要单独创建前端容器，也不需要 Nginx 容器。1Panel 中这个项目容器建议这样填：

| 配置项 | 推荐值 |
| --- | --- |
| 镜像 | `crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops:latest` |
| 端口 | 宿主 `8088` -> 容器 `8080` |
| 上传挂载 | `/opt/MealOps/app/uploads` -> `/app/uploads`，读写 |
| 日志挂载 | `/opt/MealOps/app/logs` -> `/app/logs`，读写 |
| 访问管理端 | `http://服务器IP:8088/login` |
| 访问用户端 | `http://服务器IP:8088/client/home` |
| 健康检查 | `http://服务器IP:8088/health` |

项目容器仍然需要连接 MySQL 和 Redis。若 MySQL、Redis 是 1Panel 应用，请把项目容器、MySQL、Redis 放到同一个 Docker 网络，并保证项目容器内能通过 `mysql:3306`、`redis:6379` 访问它们；如果服务名不同，就把下面的 `MYSQL_URL` 和 `REDIS_HOST` 改成实际容器名或网络别名。

单容器项目推荐环境变量：

```env
MYSQL_URL=jdbc:mysql://mysql:3306/reggie?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=mealops
MYSQL_PASSWORD=change-this-mysql-password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
MEALOPS_UPLOAD_DIR=/app/uploads
MEALOPS_DB_INIT_ENABLED=true
MEALOPS_DB_INIT_SEED_DATA_ENABLED=true
MEALOPS_ADMIN_BOOTSTRAP_ENABLED=true
MEALOPS_ADMIN_USERNAME=admin
MEALOPS_ADMIN_PASSWORD=admin1330
MEALOPS_ADMIN_NAME=System Admin
MEALOPS_ADMIN_PHONE=13800000000
MEALOPS_JWT_SECRET=change-this-long-random-secret
MEALOPS_JWT_TTL_HOURS=2
LOG_PATH=/app/logs
TZ=Asia/Shanghai
```

前端生产请求仍使用同源 `/api/`，一体镜像内部会自动把 `/api/employee/login` 转发到后端原始接口 `/employee/login`。Vue history 路由也由 Spring Boot 回退到 `index.html`，所以直接打开 `/login`、`/home`、`/client/home` 都应返回前端页面。

### Docker Compose 多容器部署

项目已提供 Docker Compose 一键部署，默认启动 MySQL、Redis、Spring Boot 后端和 Nginx 前端。前端请求同源 `/api`，Nginx 会自动把 `/api/` 转发到后端容器 `backend:8080`，启动后不需要再手动配置前后端地址。

默认部署结果：

| 入口 | 默认地址 | 说明 |
| --- | --- | --- |
| 管理端 | `http://localhost:8088/login` | Vue 前端，由 Nginx 托管 |
| 用户端 | `http://localhost:8088/client/home` | C 端点餐页面 |
| 后端健康检查 | `http://localhost:8080/health` | 用于确认 API 服务可用 |
| Swagger UI | `http://localhost:8080/swagger-ui.html` | 接口文档 |

Linux / macOS：

```bash
cp .env.example .env
KEEP_RUNNING=1 sh tools/docker-smoke-test.sh
```

Windows PowerShell：

```powershell
Copy-Item .env.example .env
.\tools\docker-smoke-test.ps1 -KeepRunning
```

推荐使用上面的启动自检脚本，而不是直接执行裸 `docker compose up`。脚本会按顺序检查 Compose 配置，先启动 MySQL 和 Redis 并等待它们进入 `healthy` 状态，再启动后端和前端并等待容器健康，最后检查后端 `/health`、前端 `/client/home` 和 `/login`。任一阶段失败时脚本会立即报错、打印近期日志，并保留已启动容器，不会自动关闭现场。

如只需检查 Compose 配置：

```bash
docker compose --env-file .env config
```

启动前建议先打开 `.env`，至少填写或确认下面几类变量：

```env
# 端口：宿主机访问容器的端口
FRONTEND_PORT=8088
BACKEND_PORT=8080
MYSQL_PORT=3306
REDIS_PORT=6379

# 自动挂载目录：不存在时 Docker Compose 会自动创建
MEALOPS_MYSQL_DATA_DIR=./docker-data/mysql
MEALOPS_REDIS_DATA_DIR=./docker-data/redis
MEALOPS_UPLOADS_DIR=./docker-data/uploads
MEALOPS_LOGS_DIR=./docker-data/logs

# MySQL：数据库名固定为 reggie，不要在 .env 里新增 MYSQL_DATABASE
MYSQL_ROOT_PASSWORD=change-this-mysql-password
MYSQL_USERNAME=root
MYSQL_PASSWORD=change-this-mysql-password

# Redis：留空表示不启用密码
REDIS_DATABASE=0
REDIS_PASSWORD=

# 后端 JWT：生产或长期环境必须修改
MEALOPS_JWT_SECRET=change-this-long-random-secret
MEALOPS_JWT_TTL_HOURS=2
```

Windows 路径建议使用正斜杠，例如 `D:/mealops/mysql`；Linux 服务器建议使用绝对路径，例如 `/data/mealops/mysql`。如果保留默认值，数据会自动挂载到项目目录下的 `docker-data/`，该目录已被 Git 忽略。

### 自动挂载目录

| `.env` 变量 | 默认宿主目录 | 容器路径 | 用途 |
| --- | --- | --- | --- |
| `MEALOPS_MYSQL_DATA_DIR` | `./docker-data/mysql` | `/var/lib/mysql` | MySQL 数据持久化 |
| `MEALOPS_REDIS_DATA_DIR` | `./docker-data/redis` | `/data` | Redis AOF 数据 |
| `MEALOPS_UPLOADS_DIR` | `./docker-data/uploads` | `/app/uploads` | 菜品图片等上传文件 |
| `MEALOPS_LOGS_DIR` | `./docker-data/logs` | `/app/logs` | 后端日志 |
| 固定挂载 | `./sql/schema.sql` | `/docker-entrypoint-initdb.d/01-schema.sql:ro` | 首次启动建表 |
| 固定挂载 | `./sql/data.sql` | `/docker-entrypoint-initdb.d/02-data.sql:ro` | 首次启动演示数据 |

这些目录使用宿主机 bind mount，而不是匿名卷。好处是图形化 Docker 面板、服务器备份脚本和迁移操作都能直接看到数据文件。

### 默认容器与网络

| 服务 | 镜像/构建 | 容器名 | 宿主端口 -> 容器端口 | 说明 |
| --- | --- | --- | --- | --- |
| MySQL | `mysql:8.4` | `mealops-mysql` | `${MYSQL_PORT:-3306}` -> `3306` | 数据库名固定为 `reggie` |
| Redis | `redis:7-alpine` | `mealops-redis` | `${REDIS_PORT:-6379}` -> `6379` | 默认无密码，可通过 `REDIS_PASSWORD` 启用 |
| 后端 | 根目录 `Dockerfile` | `mealops-backend` | `${BACKEND_PORT:-8080}` -> `8080` | 容器内默认连接 `mysql:3306/reggie` 和 `redis:6379` |
| 前端 | `frontend/Dockerfile` | `mealops-frontend` | `${FRONTEND_PORT:-8088}` -> `80` | Nginx 托管 Vue，并将 `/api/` 转发到 `backend:8080`；通过 `/healthz` 做容器健康检查 |

Compose 会创建默认 bridge 网络，后端通过服务名访问依赖：

- MySQL：`mysql:3306`
- Redis：`redis:6379`
- 后端：`backend:8080`

数据库名固定为 `reggie`，与 `sql/schema.sql` 和 `sql/data.sql` 保持一致。不要在 `.env` 中另行添加 `MYSQL_DATABASE`，否则可能造成初始化脚本和后端连接库名不一致。

### 图形化 Docker 面板填写参考

如果不通过命令行运行 Compose，而是在服务器面板中手动创建容器，请使用同一个 bridge 网络，并让容器名或网络别名保持为 `mysql`、`redis`、`backend`、`frontend`。重点填写以下内容：

| 容器 | 端口 | 挂载 | 必填环境变量 |
| --- | --- | --- | --- |
| MySQL | `${MYSQL_PORT:-3306}:3306` | `${MEALOPS_MYSQL_DATA_DIR}:/var/lib/mysql`；`./sql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro`；`./sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro` | `MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE=reggie`、`TZ=Asia/Shanghai` |
| Redis | `${REDIS_PORT:-6379}:6379` | `${MEALOPS_REDIS_DATA_DIR}:/data` | `REDIS_PASSWORD` 可留空 |
| 后端 | `${BACKEND_PORT:-8080}:8080` | `${MEALOPS_UPLOADS_DIR}:/app/uploads`；`${MEALOPS_LOGS_DIR}:/app/logs` | `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`、`REDIS_HOST`、`REDIS_PORT`、`REDIS_DATABASE`、`REDIS_PASSWORD`、`MEALOPS_UPLOAD_DIR`、`MEALOPS_ADMIN_USERNAME`、`MEALOPS_ADMIN_PASSWORD`、`MEALOPS_JWT_SECRET`、`LOG_PATH`、`TZ` |
| 前端 | `${FRONTEND_PORT:-8088}:80` | 无需额外挂载 | 与后端在同一网络，Nginx 默认代理 `backend:8080` |

后端手动容器推荐环境变量：

```env
MYSQL_URL=jdbc:mysql://mysql:3306/reggie?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
MYSQL_USERNAME=root
MYSQL_PASSWORD=change-this-mysql-password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
REDIS_PASSWORD=
MEALOPS_UPLOAD_DIR=/app/uploads
MEALOPS_DB_INIT_ENABLED=true
MEALOPS_DB_INIT_SEED_DATA_ENABLED=true
MEALOPS_ADMIN_BOOTSTRAP_ENABLED=true
MEALOPS_ADMIN_USERNAME=admin
MEALOPS_ADMIN_PASSWORD=admin1330
MEALOPS_ADMIN_NAME=System Admin
MEALOPS_ADMIN_PHONE=13800000000
MEALOPS_JWT_SECRET=change-this-long-random-secret
MEALOPS_JWT_TTL_HOURS=2
LOG_PATH=/app/logs
TZ=Asia/Shanghai
```

### 手动创建容器配置

优先使用上面的 Docker Compose；只有在服务器面板不支持 Compose 或必须拆分创建容器时，才按“图形化 Docker 面板填写参考”手动创建。

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
| `MEALOPS_MYSQL_DATA_DIR` | `./docker-data/mysql` | MySQL 容器挂载 | 宿主机 MySQL 数据目录 |
| `MEALOPS_REDIS_DATA_DIR` | `./docker-data/redis` | Redis 容器挂载 | 宿主机 Redis 数据目录 |
| `MEALOPS_UPLOADS_DIR` | `./docker-data/uploads` | 后端容器挂载 | 宿主机上传文件目录 |
| `MEALOPS_LOGS_DIR` | `./docker-data/logs` | 后端容器挂载 | 宿主机后端日志目录 |
| `MEALOPS_ADMIN_BOOTSTRAP_ENABLED` | `true` | 后端容器 | 是否在应用启动时创建或更新管理端初始账号 |
| `MEALOPS_ADMIN_USERNAME` | `admin` | 后端容器 | 管理端初始账号；启动时按该账号创建或更新员工记录 |
| `MEALOPS_ADMIN_PASSWORD` | `admin1330` | 后端容器 | 管理端初始密码；启动时写入 MD5 摘要，不会原文入库 |
| `MEALOPS_ADMIN_NAME` | `System Admin` | 后端容器 | 管理员显示姓名 |
| `MEALOPS_ADMIN_PHONE` | `13800000000` | 后端容器 | 管理员手机号 |
| `MEALOPS_JWT_SECRET` | `change-this-secret-before-production` | 后端容器 | JWT 签名密钥，生产环境必须修改 |
| `MEALOPS_JWT_TTL_HOURS` | `2` | 后端容器 | JWT 有效小时数 |
| `MEALOPS_LOG_LEVEL` | `INFO` | 后端容器 | 后端业务日志级别；排查 SQL/API 明细时可临时改为 `DEBUG`，生产建议保持 `INFO` |

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
| `JAVA_TOOL_OPTIONS` | `-XX:InitialRAMPercentage=20.0 -XX:MaxRAMPercentage=70.0 -Dfile.encoding=UTF-8` | 限制 JVM 按容器内存自适应分配，并固定 UTF-8 编码 |
| `TZ` | `Asia/Shanghai` | MySQL、后端容器时区 |

详细部署、重建、日志、冒烟验证和数据重置说明见 `docs/docker-deployment.md`。

### 容器异常停机排查

如果后端日志停在普通请求日志后直接中断，没有出现 `APPLICATION FAILED TO START`、Java 异常栈或 Spring shutdown 日志，通常不是应用主动退出，而是 Docker/面板从外部停止了容器。优先在服务器执行：

```bash
docker inspect MealOps --format '{{.State.Status}} exit={{.State.ExitCode}} oom={{.State.OOMKilled}} error={{.State.Error}}'
docker logs --tail 200 MealOps
```

判断方式：

- `oom=true`：容器被内存限制杀掉。优先提高容器内存，或保留镜像默认 `JAVA_TOOL_OPTIONS`，不要删除 `-XX:MaxRAMPercentage=70.0`。
- `exit=137` 且 `oom=false`：容器进程收到了外部 `SIGKILL`，常见于面板强制停止、重启超时、宿主机任务清理或旧镜像入口进程没有正确转发停止信号。新版镜像入口已使用 `exec su mealops -s /bin/sh -c 'java -jar /app/app.jar'` 启动 Java，并通过 `exec` 让 Java 进程接收停止信号。
- 日志大量出现 MyBatis `Preparing/Parameters`：说明业务日志级别是 `DEBUG`，会显著放大容器日志和 I/O 压力。生产环境保持 `MEALOPS_LOG_LEVEL=INFO`，仅排查问题时临时改为 `DEBUG`。
- 只有 logback `Missing watchable .xml` 这类提示：属于嵌套 jar 下配置扫描提示，不代表服务启动失败；当前镜像已关闭 logback 配置扫描以减少噪声。

## 初始账号与演示数据

初始化账号：

- 管理员账号默认：`admin`
- 管理员密码默认：`admin1330`
- 用户端演示手机号：`13900000000`

容器启动时会先检查当前 MySQL 连接库中是否存在 MealOps 核心表；如果 `reggie` 是空库或缺表，且 `MEALOPS_DB_INIT_ENABLED=true`，应用会自动执行内置 SQL 初始化表结构。`MEALOPS_DB_INIT_SEED_DATA_ENABLED=true` 时会同时导入演示数据，重复启动使用 `INSERT IGNORE` 避免重复写入。

容器随后会读取 `MEALOPS_ADMIN_USERNAME`、`MEALOPS_ADMIN_PASSWORD`、`MEALOPS_ADMIN_NAME`、`MEALOPS_ADMIN_PHONE` 并创建或更新该管理员账号。生产或演示部署时，推荐在 1Panel 环境变量中直接改这些值；如果不希望启动时覆盖账号，可设置 `MEALOPS_ADMIN_BOOTSTRAP_ENABLED=false`。

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
