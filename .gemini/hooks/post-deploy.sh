#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${1:-morupark-prod}"

echo "[post-deploy] namespace=${NAMESPACE}"

kubectl get deploy -n "${NAMESPACE}"
kubectl get statefulset -n "${NAMESPACE}"
kubectl get hpa -n "${NAMESPACE}"
kubectl get pods -n "${NAMESPACE}" -o wide

echo "[post-deploy] done"
