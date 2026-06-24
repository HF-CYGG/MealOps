from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "acr-publish.yml"
EXPECTED_REGISTRY = "crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com"
EXPECTED_REPOSITORY = "yyh163/mealops"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def check_workflow_exists() -> str:
    require(WORKFLOW.is_file(), "missing workflow file: .github/workflows/acr-publish.yml")
    return read(WORKFLOW)


def require_text(workflow: str, needle: str, message: str) -> None:
    require(needle in workflow, message)


def check_triggers(workflow: str) -> None:
    require_text(workflow, "workflow_dispatch:", "workflow must support manual dispatch")
    require_text(workflow, "pull_request:", "workflow must validate pull requests")
    require_text(workflow, "push:", "workflow must run on push")
    require_text(workflow, "branches:", "workflow push trigger must constrain branches")
    require_text(workflow, "tags:", "workflow push trigger must include release tags")


def check_validation_jobs(workflow: str) -> None:
    require_text(workflow, "mvn -B test", "workflow must run backend tests")
    require_text(workflow, "working-directory: frontend", "workflow must run frontend commands in frontend directory")
    require_text(workflow, "npm ci", "workflow must install frontend dependencies reproducibly")
    require_text(workflow, "npm run build", "workflow must build frontend")


def check_acr_publish(workflow: str) -> None:
    require_text(workflow, f"ACR_REGISTRY: {EXPECTED_REGISTRY}", "workflow must target the requested ACR registry")
    require_text(workflow, f"IMAGE_REPOSITORY: {EXPECTED_REPOSITORY}", "workflow must target the requested ACR repository")
    require_text(workflow, "docker/login-action@v3", "workflow must use Docker login action")
    require_text(workflow, "username: ${{ secrets.ALIYUN_ACR_USERNAME }}", "workflow must read ACR username from secrets")
    require_text(workflow, "password: ${{ secrets.ALIYUN_ACR_PASSWORD }}", "workflow must read ACR password from secrets")
    require_text(workflow, "docker/build-push-action@v6", "workflow must use Docker build-push action")
    require_text(workflow, "context: .", "workflow must build backend from repository root context")
    require_text(workflow, "file: ./Dockerfile", "workflow must build backend from root Dockerfile")
    require_text(workflow, "context: ./frontend", "workflow must build frontend from frontend context")
    require_text(workflow, "file: ./frontend/Dockerfile", "workflow must build frontend from frontend Dockerfile")
    require_text(workflow, "backend-latest", "workflow must publish a backend latest tag on main")
    require_text(workflow, "frontend-latest", "workflow must publish a frontend latest tag on main")
    require(re.search(r"prefix=backend-sha-", workflow), "workflow must publish immutable backend sha tags")
    require(re.search(r"prefix=frontend-sha-", workflow), "workflow must publish immutable frontend sha tags")


def main() -> int:
    workflow = check_workflow_exists()
    check_triggers(workflow)
    check_validation_jobs(workflow)
    check_acr_publish(workflow)
    print("GitHub Actions ACR workflow checks passed")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"GitHub Actions ACR workflow check failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
