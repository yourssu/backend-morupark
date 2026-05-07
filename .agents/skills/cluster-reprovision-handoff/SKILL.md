---
name: cluster-reprovision-handoff
description: Handle cluster reprovisioning and post-Terraform handoff for this MoruPark repository. Use when Codex needs to recover from `terraform destroy` and rebuild flows, restore kubeconfig access, bootstrap required operators, re-apply prod overlays, or verify that infrastructure, secrets, and workloads are ready after a fresh cluster or major infra recreation.
---

# Cluster Reprovision Handoff

Use this skill for cluster rebuild and post-provision handoff work.

## Quick Start

1. Read [references/rebuild-sequence.md](references/rebuild-sequence.md) for the canonical rebuild order.
2. Read [references/post-provision-checks.md](references/post-provision-checks.md) for downstream validation targets.
3. Read `ssl.json` when the request needs structured scenes, command order, or risk metadata.

## Workflow

1. Confirm whether the task is read-only review, rebuild prep, or post-rebuild recovery.
2. Inspect Terraform target path and cluster bootstrap assumptions.
3. Restore kubeconfig access before Kubernetes diagnostics.
4. Ensure required operators such as ESO are installed before app overlay apply.
5. Re-apply prod overlay only after bootstrap prerequisites exist.
6. Verify secrets, workloads, ingress, and follow-up handoff items.

## Guardrails

- Do not collapse infra provisioning, operator bootstrap, and app deployment into one unchecked step.
- Do not treat Terraform success as proof that the Kubernetes layer is ready.
- Do not skip post-rebuild kubeconfig refresh.
- Do not run overlay apply before bootstrap operators and CRDs are present.

## References

- Rebuild sequence: [references/rebuild-sequence.md](references/rebuild-sequence.md)
- Post-provision checks: [references/post-provision-checks.md](references/post-provision-checks.md)
- Structured graph: [ssl.json](ssl.json)
