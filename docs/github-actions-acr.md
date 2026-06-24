# GitHub Actions 阿里云 ACR 发布说明

本仓库通过 `.github/workflows/acr-publish.yml` 自动验证并发布 MealOps Docker 镜像到阿里云 ACR。

## 发布目标

- Registry: `crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com`
- Repository: `yyh163/mealops`
- Backend Dockerfile: `Dockerfile`
- Frontend Dockerfile: `frontend/Dockerfile`

## GitHub Secrets

在 GitHub 仓库 `HF-CYGG/MealOps` 的 `Settings -> Secrets and variables -> Actions` 中新增：

| Secret | 用途 |
| --- | --- |
| `ALIYUN_ACR_USERNAME` | 登录阿里云 ACR 的用户名 |
| `ALIYUN_ACR_PASSWORD` | 登录阿里云 ACR 的密码或访问凭据 |

不要把 ACR 密码、临时 Token 或 Docker 登录后的配置文件提交到仓库。

## 触发规则

- 推送到 `main`：运行后端测试、前端构建，并推送 `latest` 与 `sha` 标签。
- 推送到 `codex/**`：运行后端测试、前端构建，并推送 `sha` 标签，便于验证分支镜像。
- 推送 `v*` 标签：运行验证并发布带版本前缀的镜像标签。
- Pull Request 到 `main`：只运行验证，不登录 ACR，不推送镜像。
- `workflow_dispatch`：支持在 GitHub Actions 页面手动触发。

## 镜像标签

同一个 ACR repository 下使用前缀区分前后端：

| 服务 | 示例标签 |
| --- | --- |
| Backend | `backend-sha-1a2b3c4`, `backend-latest`, `backend-v1.0.0` |
| Frontend | `frontend-sha-1a2b3c4`, `frontend-latest`, `frontend-v1.0.0` |

完整镜像示例：

```text
crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops:backend-latest
crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops:frontend-latest
```

## 本地校验

提交前建议运行：

```bash
python tools/verify-github-actions.py
python tools/verify-docker-config.py
cd frontend && npm run build
mvn test
```

`tools/verify-github-actions.py` 只做静态校验，不会连接 GitHub 或 ACR；真实推送结果以 GitHub Actions 日志为准。

## 远端仓库

本地仓库如果还没有 `origin`，可配置为：

```bash
git remote add origin https://github.com/HF-CYGG/MealOps.git
```

之后推送当前分支：

```bash
git push -u origin codex/mealops-dining-demo
```
