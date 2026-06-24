# GitHub Actions 与阿里云 ACR 自动构建说明

本仓库的镜像链路分为两段：

- GitHub Actions：只做代码测试、前端构建和 Docker Compose 联调冒烟验证，不登录、不推送阿里云 ACR。
- 阿里云 ACR：使用 ACR 控制台中配置的 GitHub 构建规则，由阿里云构建服务器自动构建并写入 ACR 镜像仓库。

## GitHub Actions

workflow 文件为 `.github/workflows/docker-validate.yml`。

触发规则：

- 推送到 `main`
- 推送到 `codex/**`
- 推送 `release-v*` 标签
- Pull Request 到 `main`
- 手动 `workflow_dispatch`

执行内容：

- `mvn -B test`
- `cd frontend && npm ci && npm run build`
- 运行 `tools/docker-smoke-test.sh`，通过 Docker Compose 构建并启动完整栈
- 验证后端 `/health`、前端 `/client/home` 和 `/login` 可访问

GitHub Actions 不使用 `docker/build-push-action` 推送镜像，而是在同一个 job 内运行 `sh tools/docker-smoke-test.sh`，通过 Docker Compose 构建并拉起 MySQL、Redis、后端和前端后执行联调冒烟验证。

## 阿里云 ACR 自动构建

ACR 镜像仓库：

```text
crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com/yyh163/mealops
```

当前已在阿里云 ACR 控制台配置构建规则：

| GitHub 规则 | 构建上下文 | Dockerfile | 镜像版本 |
| --- | --- | --- | --- |
| `branches:main` | `/` | `Dockerfile` | `latest` |
| `tags:release-v$version` | `/` | `Dockerfile` | `$version` |

因此：

- 推送 `main` 后，由阿里云 ACR 自动构建并写入 `:latest`。
- 推送 `release-v1.0.0` 后，由阿里云 ACR 自动构建并写入 `:1.0.0`。

## 本地校验

提交前建议运行：

```bash
python tools/verify-github-actions.py
python tools/verify-docker-config.py
cd frontend && npm run build
mvn test
```

`tools/verify-github-actions.py` 会检查 GitHub Actions 是否只做 Docker build 验证，并防止重新引入 `docker/login-action`、`ALIYUN_ACR_*` 或 `push: true`。

## 注意事项

- GitHub 仓库不需要配置 `ALIYUN_ACR_USERNAME` 或 `ALIYUN_ACR_PASSWORD`。
- ACR 凭据和构建权限由阿里云 ACR 控制台管理，不进入 GitHub Actions。
- 如果需要让前端镜像也由 ACR 自动构建，需要在 ACR 中另建仓库或规则，构建上下文设为 `frontend/`，Dockerfile 设为 `frontend/Dockerfile`。
