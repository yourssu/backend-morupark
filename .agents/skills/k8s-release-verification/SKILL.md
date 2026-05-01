---
name: k8s-release-verification
description: Verify Kubernetes release state for this MoruPark repository. Use when Codex needs to check `kubectl diff/apply` readiness, review post-deploy workload health, validate namespace and overlay consistency, inspect HPA, Deployment, or StatefulSet status, or diagnose rollout failures such as `ImagePullBackOff`, `CreateContainerConfigError`, probe errors, and image tag mismatches.
---

# K8s Release Verification

Use this skill when a Kubernetes deploy needs verification or rollout troubleshooting.

## Quick Start

1. Read [references/release-checklist.md](references/release-checklist.md) for the standard verification order.
2. Read [references/common-failures.md](references/common-failures.md) for recurring rollout failure patterns.
3. Read `ssl.json` when the request needs structured scene flow or grounded diagnostics.

## Workflow

1. Confirm the target environment and overlay path.
2. Review whether the task is pre-apply validation, post-deploy verification, or failure diagnosis.
3. Inspect namespace, image tag, and resource-kind consistency before mutation.
4. Run the repo-standard post-deploy checks after apply.
5. If workloads are unhealthy, classify the failure as secret, image, probe, config, or controller mismatch.
6. End with the smallest safe next action or the relevant owner handoff.

## Guardrails

- Do not mutate Kubernetes resources without namespace or approved overlay context.
- Do not assume Redis is a Deployment in this repo; it is StatefulSet-based.
- Do not blame application code before ruling out secret sync, image pull, and probe configuration issues.
- Do not skip post-deploy verification after a successful apply.

## References

- Release checklist: [references/release-checklist.md](references/release-checklist.md)
- Common failures: [references/common-failures.md](references/common-failures.md)
- Structured graph: [ssl.json](ssl.json)
