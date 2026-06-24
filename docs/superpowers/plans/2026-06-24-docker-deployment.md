# MealOps Docker Deployment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 MealOps 改造成可通过 Docker Compose 一键部署的前后端分离应用。

**Architecture:** 使用 Compose 编排 MySQL、Redis、Spring Boot 后端和 Nginx 前端四个服务。后端通过环境变量连接容器内 MySQL/Redis，前端容器托管 Vite 构建产物并把 `/api` 反向代理到后端。

**Tech Stack:** Docker Compose, MySQL 8.4, Redis 7, Eclipse Temurin JDK/JRE 17, Node 22, Nginx 1.27, Spring Boot 3.5, Vite 8.

## Global Constraints

- 不改下单、历史订单、堂食 session 等业务逻辑。
- 不新增 Java/Node 业务依赖。
- 不提交密钥；生产部署必须通过 `.env` 覆盖 JWT、MySQL、Redis 密码。
- 保留现有 `sql/schema.sql` 与 `sql/data.sql` 作为数据库初始化来源。
- 上传文件需要持久化到 Docker volume。

---

### Task 1: Container Build Files

**Files:**
- Create: `Dockerfile`
- Create: `frontend/Dockerfile`
- Create: `.dockerignore`
- Create: `frontend/nginx.conf`

**Interfaces:**
- Backend image exposes port `8080` and runs `/app/app.jar`.
- Frontend image exposes port `80`, serves `/usr/share/nginx/html`, and proxies `/api/*` to `backend:8080`.

- [ ] **Step 1: Create backend multi-stage Dockerfile**

Use Maven build stage and JRE runtime stage. Copy the repository, run `mvn -DskipTests package`, then copy the built jar into the runtime image.

- [ ] **Step 2: Create frontend multi-stage Dockerfile**

Use Node build stage with `npm ci` and `npm run build`, then copy `dist` into Nginx.

- [ ] **Step 3: Create Nginx SPA/proxy config**

Serve Vue history routes with `try_files`, proxy `/api/` to `backend:8080/`, and expose `/healthz`.

- [ ] **Step 4: Create docker ignore files**

Exclude build outputs, dependency directories, logs, git metadata, IDE files, and local uploads.

### Task 2: Compose Runtime

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`

**Interfaces:**
- Public frontend URL: `http://localhost:${FRONTEND_PORT:-80}`
- Backend internal URL: `http://backend:8080`
- MySQL service name: `mysql`
- Database name: `reggie`
- Redis service name: `redis`
- Upload volume mount: `/app/uploads`

- [ ] **Step 1: Define MySQL service**

Use `mysql:8.4`, initialize fixed database `reggie` from `./sql`, persist data in a named volume, and expose a healthcheck.

- [ ] **Step 2: Define Redis service**

Use `redis:7-alpine`, optional password through `REDIS_PASSWORD`, persist data in a named volume, and expose a healthcheck.

- [ ] **Step 3: Define backend service**

Build from root Dockerfile, depend on healthy MySQL/Redis, pass `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `MEALOPS_UPLOAD_DIR`, `MEALOPS_JWT_SECRET`, and `LOG_PATH`.

- [ ] **Step 4: Define frontend service**

Build from `frontend/Dockerfile`, depend on backend, publish `FRONTEND_PORT:80`.

### Task 3: Deployment Documentation

**Files:**
- Modify: `README.md`
- Create: `docs/docker-deployment.md`

**Interfaces:**
- Document first-run command: `docker compose --env-file .env up -d --build`
- Document default accounts from seed data.
- Document verification URLs: `/health`, `/login`, `/client/home`, `/swagger-ui.html`.

- [ ] **Step 1: Add quick Docker section to README**

Keep it short and link to detailed documentation.

- [ ] **Step 2: Add detailed deployment doc**

Cover prerequisites, `.env` creation, startup, logs, rebuilding, data reset, upload persistence, and troubleshooting.

### Task 4: Validation

**Files:**
- Test current Docker-related files and existing app build.
- Create: `tools/verify-docker-config.py`

- [ ] **Step 1: Run local frontend build**

Run: `npm run build` in `frontend`.
Expected: exit code `0`; warnings are acceptable if they match existing Vite/Rolldown warnings.

- [ ] **Step 2: Run backend tests**

Run: `mvn test`.
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Validate Docker files if Docker is available**

Run: `python tools/verify-docker-config.py`.
Expected: Docker deployment configuration checks pass. If Docker CLI is installed, the script also runs `docker compose --env-file .env.example config`.

- [ ] **Step 4: Build images if Docker is available**

Run: `docker compose build`.
Expected: backend and frontend images build.

## Self-Review

- Spec coverage: Docker build, runtime compose, database initialization, Redis, upload persistence, and documentation are covered.
- Placeholder scan: no placeholder markers remain.
- Consistency: service names `mysql`, `redis`, `backend`, and `frontend` are consistent across Dockerfile, Nginx, Compose, and docs.
