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

## 数据库初始化

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p reggie < sql/data.sql
```

## 启动方式速览

| 场景 | 适用对象 | 启动命令 | 主要访问地址 |
| --- | --- | --- | --- |
| 根目录联动启动 | 默认本地开发，前后端一起启动 | `npm install && npm run dev` | `http://localhost:5173/login` |
| 后端本地启动 | 只调接口、Swagger、静态联调页 | `mvn spring-boot:run` | `http://localhost:8080/health` |
| 前端本地开发 | 调试 Vue 页面与路由 | `cd frontend && npm install && npm run dev` | `http://localhost:5173/login` |
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
- 第一次使用时只需要在根目录执行一次 `npm install`，用于安装联动启动依赖 `concurrently`。
- 如果本地 MySQL 需要密码，请先在当前终端设置 `MYSQL_USERNAME` 与 `MYSQL_PASSWORD`，否则后端仍可能因数据库连接失败而退出。
- 如果前端 `5173` 或后端 `8080` 被占用，启动日志会直接提示对应端口冲突。

### 1. 启动后端

如果本地 MySQL 设置了密码，建议先在当前终端注入环境变量后再启动：

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
