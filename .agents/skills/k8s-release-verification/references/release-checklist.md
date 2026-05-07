# Release Checklist

Use this reference for standard Kubernetes release verification in this repo.

## Pre Apply

- confirm target overlay: `infra/k8s/overlays/dev` or `infra/k8s/overlays/prod`
- confirm namespace consistency
- confirm image tag strategy matches the environment
- confirm repo guardrails for mutation path

## Post Apply

Run the standard post-deploy checks:

- `kubectl get deploy -n <namespace>`
- `kubectl get statefulset -n <namespace>`
- `kubectl get hpa -n <namespace>`
- `kubectl get pods -n <namespace> -o wide`

## Repo-Specific Watchpoints

- HPA target names must match the intended services
- Redis must be checked as a StatefulSet
- image tag drift can look like app failure but is a release-input issue
- secret-related failures must be separated from image or probe issues
