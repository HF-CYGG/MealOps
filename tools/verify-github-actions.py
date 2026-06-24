from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
WORKFLOW = ROOT / ".github" / "workflows" / "docker-validate.yml"
REMOVED_WORKFLOW = ROOT / ".github" / "workflows" / "acr-publish.yml"
EXPECTED_REGISTRY = "crpi-9gmsq2s17re73ia9.cn-qingdao.personal.cr.aliyuncs.com"
EXPECTED_REPOSITORY = "yyh163/mealops"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def check_workflow_exists() -> str:
    require(WORKFLOW.is_file(), "missing workflow file: .github/workflows/docker-validate.yml")
    require(not REMOVED_WORKFLOW.exists(), "old ACR push workflow must be removed")
    return read(WORKFLOW)


def require_text(workflow: str, needle: str, message: str) -> None:
    require(needle in workflow, message)


def check_triggers(workflow: str) -> None:
    require_text(workflow, "workflow_dispatch:", "workflow must support manual dispatch")
    require_text(workflow, "pull_request:", "workflow must validate pull requests")
    require_text(workflow, "push:", "workflow must run on push")
    require_text(workflow, "branches:", "workflow push trigger must constrain branches")
    require_text(workflow, "tags:", "workflow push trigger must include release tags")
    require_text(workflow, "release-v*", "workflow must align with Aliyun ACR release tag rule")


def check_validation_jobs(workflow: str) -> None:
    require_text(workflow, "mvn -B test", "workflow must run backend tests")
    require_text(workflow, "working-directory: frontend", "workflow must run frontend commands in frontend directory")
    require_text(workflow, "npm ci", "workflow must install frontend dependencies reproducibly")
    require_text(workflow, "npm run build", "workflow must build frontend")


def check_docker_validation(workflow: str) -> None:
    require_text(workflow, "docker/build-push-action@v6", "workflow must use Docker build-push action")
    require_text(workflow, "context: .", "workflow must build backend from repository root context")
    require_text(workflow, "file: ./Dockerfile", "workflow must build backend from root Dockerfile")
    require_text(workflow, "context: ./frontend", "workflow must build frontend from frontend context")
    require_text(workflow, "file: ./frontend/Dockerfile", "workflow must build frontend from frontend Dockerfile")
    require_text(workflow, "push: false", "GitHub Actions must build Docker images without pushing them")
    require("docker/login-action" not in workflow, "GitHub Actions must not log in to Aliyun ACR")
    require("ALIYUN_ACR_" not in workflow, "GitHub Actions must not require Aliyun ACR secrets")
    require(f"ACR_REGISTRY: {EXPECTED_REGISTRY}" not in workflow, "GitHub Actions must not target ACR directly")
    require(f"IMAGE_REPOSITORY: {EXPECTED_REPOSITORY}" not in workflow, "GitHub Actions must not push the ACR repository")
    require("push: true" not in workflow, "GitHub Actions must not push Docker images")


def main() -> int:
    workflow = check_workflow_exists()
    check_triggers(workflow)
    check_validation_jobs(workflow)
    check_docker_validation(workflow)
    print("GitHub Actions Docker validation workflow checks passed")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"GitHub Actions ACR workflow check failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
