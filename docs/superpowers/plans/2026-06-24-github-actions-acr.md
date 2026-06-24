# GitHub Actions ACR Publish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a GitHub Actions pipeline that validates MealOps and publishes backend and frontend Docker images to Aliyun ACR.

**Architecture:** Keep the existing backend and frontend Dockerfiles as the build source of truth. The workflow runs backend tests and frontend build first, then logs in to Aliyun ACR and pushes two tagged images into the requested ACR repository.

**Tech Stack:** GitHub Actions, Docker Buildx, Aliyun ACR, Maven/Java 17, Node.js 22, Python stdlib static verification.

## Global Constraints

- Do not commit or print any ACR credential; use GitHub Actions secrets only.
- ACR registry must be `crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com`.
- ACR repository must be `yyh163/mealops`.
- Backend image must build from root `Dockerfile`; frontend image must build from `frontend/Dockerfile`.
- Do not add new project runtime dependencies.

---

### Task 1: Workflow Static Verification

**Files:**
- Create: `tools/verify-github-actions.py`

**Interfaces:**
- Consumes: `.github/workflows/acr-publish.yml`
- Produces: `python tools/verify-github-actions.py` exit code 0 only when the ACR workflow has the required registry, secrets, build contexts, image tags, and trigger shape.

- [x] **Step 1: Write the failing static verification script**

```python
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "acr-publish.yml"

def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)
```

- [x] **Step 2: Run verification and confirm RED**

Run: `python tools\verify-github-actions.py`
Expected: FAIL with `missing workflow file: .github/workflows/acr-publish.yml`.

### Task 2: ACR Publish Workflow

**Files:**
- Create: `.github/workflows/acr-publish.yml`

**Interfaces:**
- Consumes: GitHub secrets `ALIYUN_ACR_USERNAME` and `ALIYUN_ACR_PASSWORD`
- Produces: A workflow named `Publish Docker Images to Aliyun ACR`

- [x] **Step 1: Add workflow triggers**

Use push on `main` and `codex/**`, tags `v*`, pull requests to `main`, and manual `workflow_dispatch`.

- [x] **Step 2: Add validation jobs**

Backend validation runs `mvn -B test`. Frontend validation runs `npm ci` and `npm run build` in `frontend`.

- [x] **Step 3: Add Docker publish job**

Use Docker Buildx, `docker/login-action@v3`, `docker/metadata-action@v5`, and `docker/build-push-action@v6`. Push backend tags prefixed with `backend-` and frontend tags prefixed with `frontend-` to the configured ACR repository.

### Task 3: Documentation

**Files:**
- Create: `docs/github-actions-acr.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: workflow secrets and ACR repository naming
- Produces: clear setup instructions for repository remote, required secrets, tags, and verification commands.

- [x] **Step 1: Document required secrets**

Document `ALIYUN_ACR_USERNAME` and `ALIYUN_ACR_PASSWORD`.

- [x] **Step 2: Document image tags**

Document `backend-sha-*`, `backend-latest`, `frontend-sha-*`, and `frontend-latest`.

### Task 4: Verification and Push

**Files:**
- Modify only through git metadata and remote configuration after user/credential availability.

**Interfaces:**
- Consumes: local git auth and network access
- Produces: branch pushed to `https://github.com/HF-CYGG/MealOps`

- [ ] **Step 1: Run local verification**

Run `python tools\verify-github-actions.py`, `python tools\verify-docker-config.py`, `npm run build` in `frontend`, and `mvn test`.

- [ ] **Step 2: Configure remote**

Run `git remote add origin https://github.com/HF-CYGG/MealOps.git` if no origin exists.

- [ ] **Step 3: Push**

Push the current branch or the requested branch after authentication is available.
