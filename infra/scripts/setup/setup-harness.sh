#!/usr/bin/env bash
set -euo pipefail

echo "[setup-harness] infra harness bootstrap"

require_bin() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "[setup-harness][error] missing command: $1"
    exit 1
  fi
}

require_bin gcloud
require_bin kubectl
require_bin terraform

chmod +x .gemini/hooks/pre-apply.sh || true
chmod +x .gemini/hooks/post-deploy.sh || true

echo "[setup-harness] validating expected infra paths"
for p in k8s/overlays/dev k8s/overlays/prod terraform/environments/prod; do
  if [[ ! -d "$p" ]]; then
    echo "[setup-harness][error] required path not found: $p"
    exit 1
  fi
done

PROJECT_ID="$(gcloud config get-value project 2>/dev/null || true)"
echo "[setup-harness] gcloud project=${PROJECT_ID:-unset}"

echo "[setup-harness] ready"
