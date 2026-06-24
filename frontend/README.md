# MealOps 前端说明

本文件用于说明 `frontend/` 目录下 Vue 3 + Vite 前端的本地启动、构建与访问方式。

## 技术栈

- Vue 3
- Vite 8
- Vue Router
- Pinia
- Element Plus
- Axios

## 启动前准备

前端默认通过 `/api` 代理访问本地后端 `http://localhost:8080`，因此启动前请先确保后端已经运行。

后端推荐先执行：

```bash
mvn spring-boot:run
```

后端验证地址：

- 健康检查：`http://localhost:8080/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

## 本地开发启动

在 `frontend/` 目录执行：

```bash
npm install
npm run dev
```

启动后默认入口：

- 管理端：`http://localhost:5173/login`
- 用户端：`http://localhost:5173/client/home`

说明：

- `npm run dev` 会在终端额外输出管理端和用户端入口地址。
- 开发环境下，前端请求会由 Vite 自动代理到 `http://localhost:8080`。
- 如果后端未启动，页面可以打开，但接口数据无法正常加载。

## 生产构建

```bash
npm run build
```

构建产物输出到 `frontend/dist/`，供 Nginx 或 Docker 镜像部署使用。

## Docker 运行说明

Docker 部署时，前端由 `frontend/Dockerfile` 构建，并通过 `frontend/nginx.conf` 将 `/api/` 请求转发到 `backend:8080`。

Docker 默认入口：

- 管理端：`http://localhost:8088/login`
- 用户端：`http://localhost:8088/client/home`

## 路由说明

- 管理端登录：`/login`
- 管理端首页：`/home`
- 用户端登录：`/client/login`
- 用户端首页：`/client/home`
- 用户端下单：`/client/order/submit`
- 用户端历史订单：`/client/order/history`

## 相关文件

- `vite.config.js`：Vite 配置、开发代理与启动提示
- `src/router/index.js`：前端路由定义
- `src/utils/request.js`：Axios 请求封装
- `Dockerfile`：前端容器镜像构建文件
- `nginx.conf`：生产环境路由回退与接口代理配置