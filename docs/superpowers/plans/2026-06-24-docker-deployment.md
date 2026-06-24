# MealOps Docker Deployment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 灏?MealOps 鏀归€犳垚鍙€氳繃 Docker Compose 涓€閿儴缃茬殑鍓嶅悗绔垎绂诲簲鐢ㄣ€?
**Architecture:** 浣跨敤 Compose 缂栨帓 MySQL銆丷edis銆丼pring Boot 鍚庣鍜?Nginx 鍓嶇鍥涗釜鏈嶅姟銆傚悗绔€氳繃鐜鍙橀噺杩炴帴瀹瑰櫒鍐?MySQL/Redis锛屽墠绔鍣ㄦ墭绠?Vite 鏋勫缓浜х墿骞舵妸 `/api` 鍙嶅悜浠ｇ悊鍒板悗绔€?
**Tech Stack:** Docker Compose, MySQL 8.4, Redis 7, Eclipse Temurin JDK/JRE 17, Node 22, Nginx 1.27, Spring Boot 3.5, Vite 8.

## Global Constraints

- 涓嶆敼涓嬪崟銆佸巻鍙茶鍗曘€佸爞椋?session 绛変笟鍔￠€昏緫銆?- 涓嶆柊澧?Java/Node 涓氬姟渚濊禆銆?- 涓嶆彁浜ゅ瘑閽ワ紱鐢熶骇閮ㄧ讲蹇呴』閫氳繃 `.env` 瑕嗙洊 JWT銆丮ySQL銆丷edis 瀵嗙爜銆?- 淇濈暀鐜版湁 `sql/schema.sql` 涓?`sql/data.sql` 浣滀负鏁版嵁搴撳垵濮嬪寲鏉ユ簮銆?- 涓婁紶鏂囦欢闇€瑕佹寔涔呭寲鍒?Docker volume銆?
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

- [x] **Step 1: Create backend multi-stage Dockerfile**

Use Maven build stage and JRE runtime stage. Copy the repository, run `mvn -DskipTests package`, then copy the built jar into the runtime image.

- [x] **Step 2: Create frontend multi-stage Dockerfile**

Use Node build stage with `npm ci` and `npm run build`, then copy `dist` into Nginx.

- [x] **Step 3: Create Nginx SPA/proxy config**

Serve Vue history routes with `try_files`, proxy `/api/` to `backend:8080/`, and expose `/healthz`.

- [x] **Step 4: Create docker ignore files**

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

- [x] **Step 1: Define MySQL service**

Use `mysql:8.4`, initialize fixed database `reggie` from `./sql`, persist data in a named volume, and expose a healthcheck.

- [x] **Step 2: Define Redis service**

Use `redis:7-alpine`, optional password through `REDIS_PASSWORD`, persist data in a named volume, and expose a healthcheck.

- [x] **Step 3: Define backend service**

Build from root Dockerfile, depend on healthy MySQL/Redis, pass `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `MEALOPS_UPLOAD_DIR`, `MEALOPS_JWT_SECRET`, and `LOG_PATH`.

- [x] **Step 4: Define frontend service**

Build from `frontend/Dockerfile`, depend on backend, publish `FRONTEND_PORT:80`.

### Task 3: Deployment Documentation

**Files:**
- Modify: `README.md`
- Create: `docs/docker-deployment.md`

**Interfaces:**
- Document first-run command: `docker compose --env-file .env up -d --build`
- Document default accounts from seed data.
- Document verification URLs: `/health`, `/login`, `/client/home`, `/swagger-ui.html`.

- [x] **Step 1: Add quick Docker section to README**

Keep it short and link to detailed documentation.

- [x] **Step 2: Add detailed deployment doc**

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
