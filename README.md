# MealOps

MealOps 是一个 Spring Boot 3 外卖点餐实训项目，包含管理端和用户端接口、MySQL 业务表、Redis 登录安全组件、Swagger/OpenAPI 文档和静态联调页面。

## 技术栈

- Java 17
- Spring Boot 3.5.9
- MyBatis-Plus 3.5.16
- MySQL 8.x
- Redis 6+
- Springdoc OpenAPI 2.8.17

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

## Docker 部署

项目已提供 Docker Compose 部署文件，包含 MySQL、Redis、Spring Boot 后端和 Nginx 前端。

```bash
cp .env.example .env
docker compose --env-file .env up -d --build
```

## GitHub Actions / ACR 发布

仓库提供 `.github/workflows/acr-publish.yml`，会在推送到 `main`、`codex/**` 或 `v*` 标签时运行后端测试、前端构建，并将前后端 Docker 镜像推送到：

```text
crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops
```

需要在 GitHub Actions Secrets 中配置 `ALIYUN_ACR_USERNAME` 和 `ALIYUN_ACR_PASSWORD`。详细说明见 `docs/github-actions-acr.md`。

默认访问地址：

- 用户端：`http://localhost:8088/client/home`
- 管理端：`http://localhost:8088/login`
- 后端健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

详细部署、重建、日志和数据重置说明见 `docs/docker-deployment.md`。

初始化账号：

- 管理员：`admin`
- 密码：`admin1330`
- 数据库密码字段：`849199be4c873a9ab895576681da12f3`

示例数据包含分类、菜品、口味、套餐、用户、地址、购物车、订单和操作日志。

## 启动

```bash
mvn spring-boot:run
```

启动后访问：

- 健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- 管理端静态联调页：`http://localhost:8080/static/admin.html`
- 用户端静态联调页：`http://localhost:8080/static/user.html`

## 静态联调页

`src/main/resources/static/admin.html` 和 `src/main/resources/static/user.html` 使用原生 JavaScript 调用后端接口，并把响应 JSON 直接输出到页面。页面会把登录 token 保存到 `localStorage`，请求时同时携带 `Authorization: Bearer <token>` 和 `token` 请求头，便于兼容不同拦截器实现。

## 文档留痕

- `docs/api-checklist.md`：接口验收清单
- `docs/test-record-template.md`：测试记录模板
- `docs/report-screenshot-checklist.md`：实训报告截图清单

## 目录说明

```text
sql/
  schema.sql              MySQL 表结构
  data.sql                初始化数据
src/main/resources/
  application.yml         应用配置
  logback-spring.xml      日志配置
  static/
    admin.html            管理端原生 JS 联调页
    user.html             用户端原生 JS 联调页
docs/
  api-checklist.md
  test-record-template.md
  report-screenshot-checklist.md
```
