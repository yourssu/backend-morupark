#!/usr/bin/env bash
set -euo pipefail

CMD="${1:-}"

if [[ -z "$CMD" ]]; then
  echo "[pre-apply] no command context. skip"
  exit 0
fi

echo "[pre-apply] validating: $CMD"

# Block unsafe defaults
if [[ "$CMD" == *"-auto-approve"* ]]; then
  echo "[pre-apply][error] '-auto-approve' is not allowed in this harness"
  exit 1
fi

# Kubernetes guardrails
if [[ "$CMD" == *"kubectl apply"* ]] || [[ "$CMD" == *"kubectl patch"* ]] || [[ "$CMD" == *"kubectl delete"* ]]; then
  if [[ "$CMD" != *" -n "* ]] && [[ "$CMD" != *"--namespace"* ]] && [[ "$CMD" != *" -k infra/k8s/overlays/"* ]]; then
    echo "[pre-apply][error] kubectl mutation must include namespace or approved overlay path"
    exit 1
  fi

  if [[ "$CMD" == *" -k "* ]]; then
    if [[ "$CMD" != *"infra/k8s/overlays/dev"* ]] && [[ "$CMD" != *"infra/k8s/overlays/prod"* ]]; then
      echo "[pre-apply][error] only infra/k8s/overlays/dev|prod are allowed for -k mutations"
      exit 1
    fi
  fi

  if [[ "$CMD" == *"redis"* ]] && [[ "$CMD" == *"kind: Deployment"* ]]; then
    echo "[pre-apply][error] redis is StatefulSet-based in this repo"
    exit 1
  fi
fi

# Terraform guardrails
if [[ "$CMD" == *"terraform apply"* ]] || [[ "$CMD" == *"terraform destroy"* ]]; then
  if [[ "$CMD" != *"infra/terraform/environments/prod"* ]] && [[ "$PWD" != *"infra/terraform/environments/prod"* ]]; then
    echo "[pre-apply][error] terraform mutation must target infra/terraform/environments/prod"
    exit 1
  fi

  if [[ "$CMD" == *"terraform apply"* ]] && [[ "$CMD" != *".tfplan"* ]]; then
    echo "[pre-apply][warn] apply without plan file detected (recommended: apply <plan.tfplan>)"
  fi
fi

echo "[pre-apply] validation passed"
