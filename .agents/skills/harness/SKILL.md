---
name: harness
description: Route repeated MoruPark infrastructure and Kubernetes operations through the local harness documentation and skills. Use when Codex needs to choose between cluster reprovisioning, ESO recurrence prevention, Kubernetes release verification, or broader infra operations, and when a request benefits from the standard runbook order and repo-specific guardrails before acting.
---

# Harness

Use this meta skill to choose the right repo-local operations skill and follow the standard harness order.

## Quick Start

1. Read [references/skill-routing.md](references/skill-routing.md) to choose the correct skill.
2. Read [references/operating-order.md](references/operating-order.md) when the task touches prod infra or cluster recovery.
3. Dispatch to the underlying skill with the smallest matching scope.

## Routing Rules

1. Use `eso-recurrence-prevention` for `ExternalSecret`, `SecretStore`, CRD, or synced Secret failures.
2. Use `cluster-reprovision-handoff` after Terraform reprovisioning, cluster recreation, or kubeconfig re-bootstrap work.
3. Use `k8s-release-verification` for rollout health, overlay verification, and post-deploy checks.
4. Use `infra-ops` for broader Terraform-centric infra work that is not primarily ESO or rollout verification.

## Guardrails

- Do not jump into prod mutation before identifying the correct operational path.
- Do not treat cluster rebuild, ESO bootstrap, and app rollout as the same task.
- Do not skip the repo-standard order when the request involves reprovisioning.

## References

- Skill routing: [references/skill-routing.md](references/skill-routing.md)
- Operating order: [references/operating-order.md](references/operating-order.md)
