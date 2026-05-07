---
name: eso-recurrence-prevention
description: Prevent and recover recurring External Secrets Operator failures in this MoruPark repository. Use when Codex needs to diagnose missing `ExternalSecret` or `SecretStore` CRDs, enforce the required order of `Terraform -> get-credentials -> ESO Helm -> Kustomize`, verify `morupark-gcp-secrets` synchronization, or stop repeat incidents after cluster reprovisioning or partial Kubernetes deploy failures.
---

# ESO Recurrence Prevention

Use this skill when secret synchronization or ESO bootstrap order is part of the problem.

## Quick Start

1. Read [references/eso-root-cause.md](references/eso-root-cause.md) for the recurring failure pattern.
2. Read [references/cluster-bootstrap-order.md](references/cluster-bootstrap-order.md) when the cluster was recreated or Terraform just ran.
3. Read `ssl.json` when the request needs machine-readable scene flow, recovery branching, or grounded risk metadata.

## Workflow

1. Confirm whether the issue is missing CRDs, missing ESO install, wrong deployment order, or secret sync failure.
2. Check that kubeconfig points to the intended cluster before any Kubernetes inspection.
3. Verify ESO CRDs and controller presence before applying overlays that contain `ExternalSecret` or `SecretStore`.
4. If ESO is missing, recover it with the repo-standard Helm install path and `installCRDs=true`.
5. Re-apply the prod overlay only after CRDs and ESO controller are healthy.
6. Verify `morupark-gcp-secrets`, `ExternalSecret` readiness, and dependent workload status.
7. End with the smallest prevention step needed to stop recurrence.

## Guardrails

- Do not apply `infra/k8s/overlays/prod` before confirming ESO CRDs exist.
- Do not treat Pod secret failures as an app-only issue before checking `ExternalSecret` readiness.
- Do not skip kubeconfig validation after cluster reprovisioning or timeout symptoms.
- Do not assume Secret Manager wiring is correct without checking `SecretStore`, ESO service account annotation, and synced Secret presence.

## References

- Root cause and recovery: [references/eso-root-cause.md](references/eso-root-cause.md)
- Bootstrap order and gates: [references/cluster-bootstrap-order.md](references/cluster-bootstrap-order.md)
- Structured graph: [ssl.json](ssl.json)
